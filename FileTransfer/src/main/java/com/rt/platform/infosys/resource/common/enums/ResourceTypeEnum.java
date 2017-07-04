package com.rt.platform.infosys.resource.common.enums;

/**
 * <pre>
 *
 * 【标题】: 资源类型枚举类
 * 【描述】:
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017-06-30 17:25
 * </pre>
 */
public enum ResourceTypeEnum {

    Pic(0, "picture"), Video(1, "video"), Txt(2, "txt"), Zip(3, "zip"),Other(5,"other");

    private int code;

    private String filePath;

    ResourceTypeEnum(int code, String filePath) {
        this.code = code;
        this.filePath = filePath;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static String getFilePathByCode(int code){
        for (ResourceTypeEnum typeEnum :
                ResourceTypeEnum.values()) {
            if(typeEnum.code == code){
                return typeEnum.filePath;
            }
        }
        //返回默认的
        return Other.filePath;
    }
}
