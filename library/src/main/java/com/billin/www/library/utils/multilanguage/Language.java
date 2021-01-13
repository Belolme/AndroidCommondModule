package com.billin.www.library.utils.multilanguage;

public enum Language {
    zh_CN("values-zh-rCN"), //简体
    zh_TW("values-zh-rTW"), //繁体
    en("values"),       //英语
    ko("values-ko"), //韩语
    fr("values-fr"), //法语
    de("values-de"), //德语
    pt("values-pt"), //葡萄牙语
    es("values-es"), //西班牙语
    ru("values-ru"), //俄语
    in("values-in"), //印尼语
    hi("values-hi"), //北印度语
    ar("values-ar"), //阿拉伯语
    ja("values-ja"), //日语
    th("values-th"), //泰语
    vi("values-vi"); //越南语

//    it("values-it"), //意大利语
//    tr("values-tr"), //土耳其语
//    ms("values-ms"); //马来语

//    zh_HK("values-zh-rHK"),//繁体-香港
//    en_CA("values-en-rCA"),//英语-加拿大
//    en_AU("values-en-rAU"),//英语-澳大利亚
//    en_BE("values-en-rBE"),//英语-比利时
//    en_NZ("values-en-rNZ"),//英语-新西兰
//    nl("values-nl"),//荷兰
//    de_AT("values-de-rAT"),//德语-奥地利
//    ar_KW("values-ar-rKW"),//阿拉伯语-科威特
//    ar_SA("values-ar-rSA"),//阿拉伯语-沙特阿拉伯
//    ar_IL("values-ar-rIL"),//阿拉伯语-以色列
//    en_PK("values-en-rPK"),//英语-巴基斯坦
//    en_PH("values-en-rPH"),//英语-菲律宾
//    es_MX("values-es-rMX"),//西班牙语-墨西哥
//    es_CL("values-es-rCL"),//西班牙语-智利
//    pt_BR("values-pt-rBR"),//葡萄牙语-巴西
//    zh_HK("values-zh-rHK");//繁体-香港


    private final String mDirName;

    Language(String dirName) {
        mDirName = dirName;
    }

    public static Language get(String key) {
        for (Language language : Language.values()) {
            if (language.toString().equals(key)) {
                return language;
            }
        }

        return null;
    }

    public String getDirName() {
        return mDirName;
    }
}