package io.zeymo.network.socket;

/**
 * Created with IntelliJ IDEA.
 * User: salah.liuyj
 * Date: 15-1-23
 * Time: 上午11:20
 * To change this template use File | Settings | File Templates.
 */
public enum HbType {
    LOSESERVICE((byte)1,"是否提供服务状态"),
    MASTER((byte)2,"集群master广播");

    private byte    type;
    private String 	desc;

    private HbType(byte type,String desc){
        this.type = type;
        this.desc = desc;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
