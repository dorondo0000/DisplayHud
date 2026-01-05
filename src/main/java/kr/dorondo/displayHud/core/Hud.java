package kr.dorondo.displayHud.core;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import kr.dorondo.displayHud.DisplayHud;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.bukkit.Location;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;

import javax.annotation.Nullable;

public final class Hud {
    public static final int BASE_ENTITY_ID = -69740000; // -1* size of getHuds(player)

    private static final int FLAG_ALIGN_LEFT  = 0x08;
    private static final int FLAG_ALIGN_RIGHT = 0x10;


    private Player player;
    private String id;
    private UUID uuid;

    private Integer entityId;

    private HudElement element = new HudElement(new ItemStack(Material.WHITE_CONCRETE));
    private Vector3f location = new Vector3f(0f,0f,0f);
    private Vector3f scale = new Vector3f(1f, 1f, 1f);
    private Vector3f leftRotation = new Vector3f();
    private Vector3f rightRotation = new Vector3f();
    private HudAlignment hudAlignment = HudAlignment.CENTER;
    private Integer alignmentInt = 0;
    private TextDisplay.TextAlignment textAlignment;
    private Integer textLineWidth = 200; //바꿀수잇게 하긴해야함 아마
    //실제 transform 같은거 넣어야험? 안넣어도될느낌
    //사망시 재탑승 or 삭제

    public Hud(Player player, String id, UUID uuid, HudDefinition definition) {
        this.player = Objects.requireNonNull(player, "player");
        this.id = Objects.requireNonNull(id, "id");
        this.uuid = uuid;
        this.element = definition.element();
        this.location = definition.location();
        this.scale = definition.scale();
        this.leftRotation = definition.leftRotation();
        this.rightRotation = definition.rightRotation();
        this.hudAlignment = definition.hudAlignment();
        this.textAlignment = definition.textAlignment();

        set();
    }

    public void set(){
        EntityType entitytype = (EntityTypes.ITEM_DISPLAY);

        Location blocation = player.getLocation().clone();
        blocation.setPitch(0);
        blocation.setYaw(0);
        if(element.isComponent()){
            //blocation.setYaw(180); //hud.sk 에서 안쓰던거라 주시 이거때메 getlocvector 좌우 바뀜
            entitytype = (EntityTypes.TEXT_DISPLAY);
        }

        entityId = getEmptyEntityId();

        Integer gap = DisplayHud.getInstance().getAlignmentGap();

        PacketSender.spawn(player,entityId,uuid,entitytype,blocation);
        List<EntityData<?>> metadata = new java.util.ArrayList<>(List.of());
        if(hudAlignment == HudAlignment.LEFT){
            alignmentInt = gap;
        }
        else if(hudAlignment == HudAlignment.CENTER){
            alignmentInt = gap*2;
        }
        else if(hudAlignment == HudAlignment.RIGHT){
            alignmentInt = gap*3;
        }
        if(element.isItemStack()){
            com.github.retrooper.packetevents.protocol.item.ItemStack item = SpigotConversionUtil.fromBukkitItemStack(element.getItemStack());
            metadata.add(new EntityData<>(23, EntityDataTypes.ITEMSTACK,item));
        }
        else{
            Component text = Component.text(" ".repeat(200))//첫번째줄 폰트 고정해야할수도
                    .append(Component.newline())
                    .append(element.getComponent());
            metadata.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT,text));

            //metadata.add(new EntityData<>(24, EntityDataTypes.INT,textLineWidth));

            byte flags = 0; //텍스트 배경,그림자 넣으려면 이부분 수정하고 flags 도 변수로 뺴야할듯
            switch (textAlignment) {
                case TextDisplay.TextAlignment.LEFT -> flags |= (1 << 3);
                case TextDisplay.TextAlignment.RIGHT -> flags |= (2 << 3);
            }

            metadata.add(new EntityData<>(27, EntityDataTypes.BYTE,flags));
        }
        if(element.isComponent()){
            Quaternionf quat = LayoutCalculator.vecToQuat(new Vector3f(leftRotation).add(0,180,0));
            metadata.add(new EntityData<>(13, EntityDataTypes.QUATERNION,convertQuat(quat)));
        }
        metadata.add(new EntityData<>(16, EntityDataTypes.INT,255));

        Vector3f sv = LayoutCalculator.getScaleVector(scale);
        metadata.add(new EntityData<>(12, EntityDataTypes.VECTOR3F,convertVector(sv)));

        Vector3f lv = LayoutCalculator.getLocationVector(location,scale,this);
        lv.y -= alignmentInt;
        metadata.add(new EntityData<>(11, EntityDataTypes.VECTOR3F,convertVector(lv)));


        update(metadata);
        mount();
    }

    public void dispose() {
        PacketSender.remove(player,entityId);
    }

    public void update(List<EntityData<?>> metadata) {
        PacketSender.update(player,entityId,metadata);
    }


    public void mount() {
        PacketSender.mount(player,player.getEntityId(),entityId);
    }

    public Integer getEmptyEntityId() {
        int cnt = HudRegistry.getInstance().getHuds(player).size();
        return BASE_ENTITY_ID - cnt;
    }

    public String getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public HudElement getElement() {
        return element;
    }

    public Integer getTextLineWidth() {
        return textLineWidth;
    }

    public TextDisplay.TextAlignment getTextAlignment() {
        return textAlignment;
    }

    public void setLocation(Vector3f location){
        setLocation(location,0);
    }
    public void setLocation(Vector3f location,Integer time) {
        Vector3f lv = LayoutCalculator.getLocationVector(location,scale,this);
        lv.y -= alignmentInt;
        List<EntityData<?>> metadata = new java.util.ArrayList<>(List.of());
        metadata.add(new EntityData<>(11, EntityDataTypes.VECTOR3F,convertVector(lv)));
        metadata.add(new EntityData<>(8, EntityDataTypes.INT,0));
        metadata.add(new EntityData<>(9, EntityDataTypes.INT,time));
        update(metadata);
    }

    public Vector3f getLocation() {
       return location;
    }

    public void setScale(Vector3f vector){
        setScale(vector,0);
    }
    public void setScale(Vector3f vector,Integer time) {
        Vector3f sv = LayoutCalculator.getScaleVector(vector);
        List<EntityData<?>> metadata = new java.util.ArrayList<>(List.of());
        metadata.add(new EntityData<>(12, EntityDataTypes.VECTOR3F,convertVector(sv)));
        metadata.add(new EntityData<>(8, EntityDataTypes.INT,0));
        metadata.add(new EntityData<>(9, EntityDataTypes.INT,time));
        update(metadata);
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setLeftRotation(Vector3f vector){
        setLeftRotation(vector,0);
    }
    public void setLeftRotation(Vector3f ogvector,Integer time) {
        Vector3f vector = new Vector3f(ogvector);
        if(element.isComponent()) vector.add(0,180,0);
        Quaternionf quat = LayoutCalculator.vecToQuat(vector);
        List<EntityData<?>> metadata = new java.util.ArrayList<>(List.of());
        metadata.add(new EntityData<>(13, EntityDataTypes.QUATERNION,convertQuat(quat)));
        metadata.add(new EntityData<>(8, EntityDataTypes.INT,0));
        metadata.add(new EntityData<>(9, EntityDataTypes.INT,time));
        update(metadata);
    }
    //getLeftRotate

    public void setRightRotation(Vector3f vector){
        setRightRotation(vector,0);
    }
    public void setRightRotation(Vector3f vector,Integer time) {
        Quaternionf quat = LayoutCalculator.vecToQuat(vector);
        List<EntityData<?>> metadata = new java.util.ArrayList<>(List.of());
        metadata.add(new EntityData<>(14, EntityDataTypes.QUATERNION,convertQuat(quat)));
        metadata.add(new EntityData<>(8, EntityDataTypes.INT,0));
        metadata.add(new EntityData<>(9, EntityDataTypes.INT,time));
        update(metadata);
    }
    //getRightRotate

    //setText

    //getText

    //setItem

    //getItem

    public com.github.retrooper.packetevents.util.Vector3f convertVector(Vector3f vector) {
        return new com.github.retrooper.packetevents.util.Vector3f(vector.x,vector.y,vector.z);

    }

    public com.github.retrooper.packetevents.util.Quaternion4f convertQuat(Quaternionf quat) {
        return new com.github.retrooper.packetevents.util.Quaternion4f(quat.x,quat.y,quat.z,quat.w);

    }





}
