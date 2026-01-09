package kr.dorondo.displayHud.core;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.item.ItemDisplayContext;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.TextDisplay.TextAlignment;
import com.mojang.math.Transformation;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public abstract class DisplayHud {
    public enum HudAlignment {
        CENTER,
        LEFT,
        RIGHT,
        UNALIGNED
    }

    private static WeakHashMap<Player, Map<String, DisplayHud>> HudRegistry = new WeakHashMap<>();

    public static DisplayHud getHud(Player player, String id) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(id, "id");

        Map<String, DisplayHud> huds = HudRegistry.get(player);
        if (huds == null) {
            return null;
        }
        return huds.get(id);
    }

    public static Map<String, DisplayHud> getHuds(Player player) {
        Objects.requireNonNull(player, "player");

        Map<String, DisplayHud> huds = HudRegistry.get(player);
        if (huds == null || huds.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(huds);
    }

    public static void removeHud(Player player,String id){
        DisplayHud hud = getHud(player,id);
        if (hud!=null) hud.remove();
    }

    public static void clearHuds(Player player) {
        Objects.requireNonNull(player, "player");
        Map<String, DisplayHud> huds = HudRegistry.remove(player);
        if(huds == null)return;
        huds.values().forEach(DisplayHud::remove);
    }

    protected Player player;
    protected String id;
    protected UUID uuid;

    protected Display NMSdisplay;
    protected Integer NMSid;

    protected Vector3f location = new Vector3f(0f,0f,0f);
    protected Vector3f scale = new Vector3f(1f, 1f, 1f);
    protected HudAlignment alignment = HudAlignment.CENTER;
    //사망시 재탑승 or 삭제 여부?

    public DisplayHud() {
        setNMSdisplay(Bukkit.getWorlds().getFirst());
        this.NMSid = getNMSdisplay().getId();
    }

    public boolean spawn(Player player, String id){
        return spawn(player,id,UUID.randomUUID());
    }

    public boolean spawn(Player player, String id, UUID uuid){
        if(this.player != null){
            return false;
        }
        Map<String, DisplayHud> map = HudRegistry.computeIfAbsent(player, v -> new HashMap<>());

        if (map.containsKey(id)) {
            return false;
        }

        map.put(id, this);

        this.player = Objects.requireNonNull(player, "player");
        this.id = Objects.requireNonNull(id, "id");
        this.uuid = uuid;

        Location blocation = player.getLocation().clone();//.add(0,-4000,0); // x,z 청크 유지한채 y만 -4000으로 내려서 탑승 보강 삭제
        blocation.setPitch(0);
        blocation.setYaw(0);

        PacketSender.spawn(player,NMSid,uuid,getEntityType(),blocation);


        setLeftRotation(getLeftRotationVector());
        setBrightness(15,15);
        setScale(scale);
        setLocation(location);
        update();
        mount();


        return true;




    }

    public void remove() {
        if(player == null) return;
        PacketSender.remove(player,NMSid);
        Map<String, DisplayHud> huds = HudRegistry.get(player);
        if (huds == null) {
            return;
        }
        huds.remove(id);
        if (huds.isEmpty()) {
            HudRegistry.remove(player);
        }
    }

    public void update(){
        if(id == null) return;
        List<SynchedEntityData.DataValue<?>> pack = getNMSdisplay().getEntityData().packDirty();
        if (pack == null) return;
        if (pack.isEmpty()) return;
        Set<Integer> dirtyIds = new HashSet<>();
        for (SynchedEntityData.DataValue<?> dv : pack) {
            dirtyIds.add(dv.id());
        }
        List<EntityData<?>> metadata = new ArrayList<>(); //좀 애매한방식이긴한데 일단쓰기
        for (EntityData<?> ed: SpigotConversionUtil.getEntityMetadata(getNMSdisplay().getBukkitEntity())){
            if (dirtyIds.contains(ed.getIndex())) {
                metadata.add(ed);
            }
        }
        PacketSender.update(player,NMSid,metadata);
    }


    public void teleport(){
        if(player == null) return;
        Location location = player.getLocation().clone();
        location.setYaw(0);
        location.setPitch(0);
        PacketSender.teleport(player,NMSid,location);
    }


    public void mount() {
        if(player == null) return;
        PacketSender.mount(player,player.getEntityId(),NMSid);
    }

    public Player getPlayer(){
        return this.player;
    }

    public String getId(){
        return this.id;
    }

    public UUID getUuid(){
        return this.uuid;
    }

    public void setNMSdisplay(World world){
        NMSdisplay = new ItemDisplay(net.minecraft.world.entity.EntityType.ITEM_DISPLAY,((CraftWorld) world).getHandle());
    }

    public Display getNMSdisplay(){
        return NMSdisplay;
    }

    public Integer getNMSid() {return NMSid;}

    public EntityType getEntityType(){
        return EntityTypes.DISPLAY;
    }

    public void setLocation(float x,float y,float z){
        setLocation(new Vector3f(x,y,z),0);
    }

    public void setLocation(float x,float y,float z,int time){
        setLocation(new Vector3f(x,y,z),time);
    }

    public void setLocation(Vector3f location){
        setLocation(location,0);
    }
    public void setLocation(Vector3f location,Integer time) {
        this.location = location;
        Vector3f lv = getLocationVector();
        lv.y -= getAlignmentInt();

        Transformation tf = Display.createTransformation(getNMSdisplay().getEntityData());
        tf = new Transformation(lv,tf.getLeftRotation(),tf.getScale(),tf.getRightRotation());
        getNMSdisplay().setTransformation(tf);
        getNMSdisplay().setTransformationInterpolationDelay(0);
        getNMSdisplay().setTransformationInterpolationDuration(time);
        update();
    }

    public Vector3f getLocation() {
        return location;
    }

    public void setScale(float x,float y,float z){
        setScale(new Vector3f(x,y,z),0);
    }

    public void setScale(float x,float y,float z,int time){
        setScale(new Vector3f(x,y,z),time);
    }

    public void setScale(Vector3f scale){
        setScale(scale,0);
    }
    public void setScale(Vector3f scale,Integer time) {
        this.scale = scale;
        Vector3f sv = getScaleVector();

        Transformation tf = Display.createTransformation(getNMSdisplay().getEntityData());
        tf = new Transformation(tf.getTranslation(),tf.getLeftRotation(),sv,tf.getRightRotation());
        getNMSdisplay().setTransformation(tf);
        getNMSdisplay().setTransformationInterpolationDelay(0);
        getNMSdisplay().setTransformationInterpolationDuration(time);
        update();
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setLeftRotation(float x,float y,float z){
        setLeftRotation(new Vector3f(x,y,z),0);
    }

    public void setLeftRotation(float x,float y,float z,int time){
        setLeftRotation(new Vector3f(x,y,z),time);
    }

    public void setLeftRotation(Vector3f vector){
        setLeftRotation(vector,0);
    }
    public void setLeftRotation(Vector3f vector,Integer time) {
        Quaternionf quat = vecToQuat(vector);
        Transformation tf = Display.createTransformation(getNMSdisplay().getEntityData());
        tf = new Transformation(tf.getTranslation(),quat,tf.getScale(),tf.getRightRotation());
        getNMSdisplay().setTransformation(tf);
        getNMSdisplay().setTransformationInterpolationDelay(0);
        getNMSdisplay().setTransformationInterpolationDuration(time);
        update();
    }
    public Quaternionf getLeftRotation(){
        return Display.createTransformation(getNMSdisplay().getEntityData()).getLeftRotation();
    }

    public Vector3f getLeftRotationVector(){
        return quatToVec(getLeftRotation());
    }

    public void setRightRotation(float x,float y,float z){
        setRightRotation(new Vector3f(x,y,z),0);
    }

    public void setRightRotation(float x,float y,float z,int time){
        setRightRotation(new Vector3f(x,y,z),time);
    }

    public void setRightRotation(Vector3f vector){
        setRightRotation(vector,0);
    }
    public void setRightRotation(Vector3f vector,Integer time) {
        Quaternionf quat = vecToQuat(vector);
        Transformation tf = Display.createTransformation(getNMSdisplay().getEntityData());
        tf = new Transformation(tf.getTranslation(),tf.getLeftRotation(),tf.getScale(),quat);
        getNMSdisplay().setTransformation(tf);
        getNMSdisplay().setTransformationInterpolationDelay(0);
        getNMSdisplay().setTransformationInterpolationDuration(time);
        update();
    }
    public Quaternionf getRightRotation(){
        return Display.createTransformation(getNMSdisplay().getEntityData()).getRightRotation();
    }

    public Vector3f getRightRotationVector(){
        return quatToVec(getRightRotation());
    }

    public void setAlignment(HudAlignment alignment){
        this.alignment = alignment;
        setLocation(location,0);
    }

    public HudAlignment getAlignment(){
        return alignment;
    }

    public Integer getAlignmentInt(){
        //get
        Integer gap = DisplayHudManager.alignmentGap;
        if(alignment == HudAlignment.LEFT){
            return gap;
        }
        else if(alignment == HudAlignment.CENTER){
            return gap*2;
        }
        else if(alignment == HudAlignment.RIGHT) {
            return gap * 3;
        }
        return 0;
    }

    public void setInterpolationDuration(Integer n){
        getNMSdisplay().setTransformationInterpolationDuration(n);
        update();
    }

    public Integer getInterpolationDuration(){
        return getNMSdisplay().getTransformationInterpolationDuration();
    }

    public void setInterpolationDelay(Integer n){
        getNMSdisplay().setTransformationInterpolationDelay(n);
        update();
    }

    public Integer getInterpolationDelay(){
        return getNMSdisplay().getTransformationInterpolationDelay();
    }

    public void setBrightness(int block, int sky){
        getNMSdisplay().setBrightnessOverride(new Brightness(block,sky));
        update();
    }

    public Integer getBrightnessBlock(){
        return getNMSdisplay().getBrightnessOverride().block();
    }

    public Integer getBrightnessSky(){
        return getNMSdisplay().getBrightnessOverride().sky();
    }

    public void setHeight(float height){
        getNMSdisplay().setHeight(height);
        update();
    }

    public float getHeight(){
        return getNMSdisplay().getHeight();
    }

    public void setWidth(float width){
        getNMSdisplay().setWidth(width);
        update();
    }

    public float getWidth(){
        return getNMSdisplay().getWidth();
    }

    public void setGlowColorOverride(int color){
        getNMSdisplay().setGlowColorOverride(color);
        update();
    }

    public int getGlowColorOverride(){
        return getNMSdisplay().getGlowColorOverride();
    }

    public void setViewRange(float viewRange){
        getNMSdisplay().setViewRange(viewRange);
        update();
    }

    public float getViewRange(){
        return getNMSdisplay().getViewRange();
    }



    public Vector3f getLocationVector(){
        return new Vector3f(location); //in child
    }

    public Vector3f getScaleVector(){
        float unitX = DisplayHudManager.unitX;
        float unitY = DisplayHudManager.unitY;
        return new Vector3f(scale.x*unitX,scale.y*unitY,scale.z*unitX);
    }

    public static Quaternionf vecToQuat(Vector3f vector){
        return new Quaternionf().rotateZYX(
                (float) Math.toRadians(vector.x),
                (float) Math.toRadians(vector.y),
                (float) Math.toRadians(vector.z)
        );
    }

    public static Vector3f quatToVec(Quaternionf quat) {
        Vector3f eulerRad = new Vector3f();
        quat.getEulerAnglesZYX(eulerRad);

        return new Vector3f(
                (float) Math.toDegrees(eulerRad.x),
                (float) Math.toDegrees(eulerRad.y),
                (float) Math.toDegrees(eulerRad.z)
        );
    }



    public static final class ItemDisplayHud extends DisplayHud{
        protected ItemDisplay NMSitemdisplay;

        public ItemDisplayHud() {
            setNMSdisplay(Bukkit.getWorlds().getFirst());
            this.NMSid = getNMSdisplay().getId();
        }

        public void setNMSdisplay(World world){
            NMSitemdisplay = new ItemDisplay(net.minecraft.world.entity.EntityType.ITEM_DISPLAY,((CraftWorld) world).getHandle());
        }

        public ItemDisplay getNMSdisplay(){
            return NMSitemdisplay;
        }

        public EntityType getEntityType(){
            return EntityTypes.ITEM_DISPLAY;
        }

        public void setItem(ItemStack itemstack){
            getNMSdisplay().setItemStack(net.minecraft.world.item.ItemStack.fromBukkitCopy(itemstack));
            setLocation(location);
            update();
        }

        public ItemStack getItem(){
            return getNMSdisplay().getItemStack().asBukkitCopy();
        }

        public void setItemTransform(ItemDisplayTransform transform){
            setItemTransform(transform.name());
        }

        public void setItemTransform(String string){
            getNMSdisplay().setItemTransform(ItemDisplayContext.valueOf(string));
            update();
        }

        //get

        @Override
        public Vector3f getLocationVector(){
            Vector3f scale = new Vector3f(this.scale);
            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;
            if(getItem().getType().isBlock()){
                offsetZ += scale.z/2;
            }
            else{
                offsetZ += scale.z/32;
            }
            double UnitX = DisplayHudManager.unitX;
            double UnitY = DisplayHudManager.unitY;
            double ScreenX = DisplayHudManager.screenX;
            double ScreenY = DisplayHudManager.screenY;
            Integer AlignmentGap = DisplayHudManager.alignmentGap;
            double newX = ( (ScreenX/2) - location.x - offsetX) * UnitX;
            double newY = -1.5*AlignmentGap -0.178+(((ScreenY/2)-(location.y)-offsetY)*UnitY);
            double newZ = (UnitX*offsetZ)-((location.z)*100);
            return new Vector3f((float) newX,(float) newY,(float) newZ);
        }
    }

    public static final class TextDisplayHud extends DisplayHud{
        protected TextDisplay NMStextdisplay;
        protected TextAlignment textAlignment = TextAlignment.CENTER;

        public TextDisplayHud() {
            setNMSdisplay(Bukkit.getWorlds().getFirst());
            this.NMSid = getNMSdisplay().getId();
        }

        public void setNMSdisplay(World world){
            NMStextdisplay = new TextDisplay(net.minecraft.world.entity.EntityType.TEXT_DISPLAY,((CraftWorld) world).getHandle());
        }

        public TextDisplay getNMSdisplay(){
            return NMStextdisplay;
        }

        public EntityType getEntityType(){
            return EntityTypes.TEXT_DISPLAY;
        }

        public void setText(String text){
            String string = " ".repeat(200) + "\n";//첫번째줄 폰트 고정해야할느낌? 항상 균일하게보이려면
            string += text;
            getNMSdisplay().setText(net.minecraft.network.chat.Component.literal(string));
            setLocation(location);
            update();
        }

        public String getText(){
            return getNMSdisplay().getText().getString();
        }

        public void setShadowToggle(boolean toggle){
            byte FLAG = 1;
            byte flag = getNMSdisplay().getFlags();
            getNMSdisplay().setFlags((byte) (toggle? (flag|FLAG) : (flag&~FLAG)));
            update();
        }
        /*
        public void setSeeThroughToggle(boolean toggle){
            byte FLAG = 2;
            byte flag = getNMSdisplay().getFlags();
            getNMSdisplay().setFlags((byte) (toggle? (flag|FLAG) : (flag&~FLAG)));
        }*/

        public void setTextAlignment(TextAlignment textAlignment){
            byte FLAG_ALIGN_LEFT = 8;
            byte FLAG_ALIGN_RIGHT = 16;
            byte flag = getNMSdisplay().getFlags();
            flag &= ~(FLAG_ALIGN_LEFT | FLAG_ALIGN_RIGHT);
            switch (textAlignment) {
                case LEFT  -> flag |= FLAG_ALIGN_LEFT;
                case RIGHT -> flag |= FLAG_ALIGN_RIGHT;
            }
            getNMSdisplay().setFlags(flag);
            this.textAlignment = textAlignment;
            update();
        }

        public void setBackGroundColor(){ //미완
            byte FLAG = 4;
            byte flag = getNMSdisplay().getFlags();
            //etNMSdisplay().setTextOpacity();
            //getNMSdisplay().setLineWidth();
            //getNMSdisplay().setFlags((byte) (flag&~FLAG));
            //getNMSdisplay().getEntityData().set(TextDisplay.INITIAL_BACKGROUND, argb);
        }

        public void setLineWidth(){ //미완
            //getNMSdisplay().setLineWidth()
        }

        public void setTextOpacity(int n){
            getNMSdisplay().setTextOpacity((byte) n);
            update();
        }

        @Override
        public void setLeftRotation(Vector3f vector,Integer time) {
            vector.add(0,180,0);
            Quaternionf quat = vecToQuat(vector);
            Transformation tf = Display.createTransformation(getNMSdisplay().getEntityData());
            tf = new Transformation(tf.getTranslation(),quat,tf.getScale(),tf.getRightRotation());
            getNMSdisplay().setTransformation(tf);
            getNMSdisplay().setTransformationInterpolationDelay(0);
            getNMSdisplay().setTransformationInterpolationDuration(time);
            update();
        }

        public Vector3f getLocationVector(){
            Vector3f scale = new Vector3f(this.scale);
            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            String text = getText();
            offsetY -= scale.y/2;
            int lines = countLinesNl(text);
            offsetY += ((scale.y / 4.0) * (lines - 1.5));
            int lineWidth = getNMSdisplay().getLineWidth();
            switch(textAlignment){
                case LEFT -> offsetX = (scale.x)*(((lineWidth)*0.0125) - 0.5);
                case RIGHT -> offsetX = (scale.x)*(((lineWidth)*-0.0125) - 0.55);
                case CENTER -> scale.x = 0;
            }
            offsetX += scale.x/2;
            offsetY += scale.y/2;

            double UnitX = DisplayHudManager.unitX;
            double UnitY = DisplayHudManager.unitY;
            double ScreenX = DisplayHudManager.screenX;
            double ScreenY = DisplayHudManager.screenY;
            Integer AlignmentGap = DisplayHudManager.alignmentGap;
            double newX = ( (ScreenX/2) - location.x - offsetX) * UnitX;
            double newY = -1.5*AlignmentGap -0.178+(((ScreenY/2)-(location.y)-offsetY)*UnitY);
            double newZ = (UnitX*offsetZ)-((location.z)*100);
            return new Vector3f((float) newX,(float) newY,(float) newZ);
        }

        private static int countLinesNl(String s) {
            return (s == null || s.isEmpty()) ? 1 : s.split("\n").length;
        }
    }

    public static final class BlockDisplayHud extends DisplayHud{
        protected BlockDisplay NMSblockdisplay;

        public BlockDisplayHud() {
            setNMSdisplay(Bukkit.getWorlds().getFirst());
            this.NMSid = getNMSdisplay().getId();
        }

        public void setNMSdisplay(World world){
            NMSblockdisplay = new BlockDisplay(net.minecraft.world.entity.EntityType.BLOCK_DISPLAY,((CraftWorld) world).getHandle());
        }

        public BlockDisplay getNMSdisplay(){
            return NMSblockdisplay;
        }

        public EntityType getEntityType(){
            return EntityTypes.BLOCK_DISPLAY;
        }

        public void setBlock(BlockState blockstate){
            getNMSdisplay().setBlockState((net.minecraft.world.level.block.state.BlockState) blockstate);
            update();
        }

        public BlockState getBlock(){
            return (BlockState) getNMSdisplay().getBlockState();
        }
        //get

        @Override
        public Vector3f getLocationVector(){
            Vector3f scale = new Vector3f(this.scale);
            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;
            offsetZ += scale.z/2; //block
            double UnitX = DisplayHudManager.unitX;
            double UnitY = DisplayHudManager.unitY;
            double ScreenX = DisplayHudManager.screenX;
            double ScreenY = DisplayHudManager.screenY;
            Integer AlignmentGap = DisplayHudManager.alignmentGap;
            double newX = ( (ScreenX/2) - location.x - offsetX) * UnitX;
            double newY = -1.5*AlignmentGap -0.178+(((ScreenY/2)-(location.y)-offsetY)*UnitY);
            double newZ = (UnitX*offsetZ)-((location.z)*100);
            return new Vector3f((float) newX,(float) newY,(float) newZ);
        }
    }

}
