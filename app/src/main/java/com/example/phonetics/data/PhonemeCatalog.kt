package com.example.phonetics.data

/**
 * 英式 DJ（Daniel Jones）音标体系共 48 个音素：20 元音 + 28 辅音。
 *
 * 数据规则（修改时必须保持）：
 * 1. [Phoneme.id] 全局唯一且仅含 ASCII 小写字母与下划线，用于录音文件名；
 * 2. 每个音标至少 2 个示例词，示例词的 [ExampleWord.phonemes] 必须只由目录中存在的 id 组成；
 * 3. 每个示例词必须确实包含所属音标（练习出题依赖此约束保证唯一正确答案）。
 */
object PhonemeCatalog {
    val all: List<Phoneme> =
        listOf(
            // ---------- 元音 · 单元音 12 ----------
            Phoneme(
                id = "i_long",
                symbol = "iː",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "前元音里的长音。嘴角向两边拉开，舌尖抵住下齿，舌前部抬到最高。",
                examples =
                    listOf(
                        ExampleWord("sheep", listOf("ʃ", "iː", "p"), "绵羊"),
                        ExampleWord("tea", listOf("t", "iː"), "茶"),
                        ExampleWord("green", listOf("g", "r", "iː", "n"), "绿色的"),
                    ),
            ),
            Phoneme(
                id = "i_short",
                symbol = "ɪ",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "iː 的短促版本。口型更松、舌位略低，不要拖长，收得干脆。",
                examples =
                    listOf(
                        ExampleWord("ship", listOf("ʃ", "ɪ", "p"), "船"),
                        ExampleWord("big", listOf("b", "ɪ", "g"), "大的"),
                        ExampleWord("sit", listOf("s", "ɪ", "t"), "坐"),
                    ),
            ),
            Phoneme(
                id = "e",
                symbol = "e",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "前元音短音。上下齿间距约一个小指尖，舌前部微微抬起。",
                examples =
                    listOf(
                        ExampleWord("bed", listOf("b", "e", "d"), "床"),
                        ExampleWord("pen", listOf("p", "e", "n"), "钢笔"),
                        ExampleWord("head", listOf("h", "e", "d"), "头"),
                    ),
            ),
            Phoneme(
                id = "ae",
                symbol = "æ",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "前元音里口型最大的短音。下颌放低，舌前部抬起，嘴角略扁。",
                examples =
                    listOf(
                        ExampleWord("cat", listOf("k", "æ", "t"), "猫"),
                        ExampleWord("hand", listOf("h", "æ", "n", "d"), "手"),
                        ExampleWord("bad", listOf("b", "æ", "d"), "坏的"),
                    ),
            ),
            Phoneme(
                id = "er_long",
                symbol = "ɜː",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "舌位居中的长音。舌身平放口中，嘴唇自然放松略带圆意。",
                examples =
                    listOf(
                        ExampleWord("bird", listOf("b", "ɜː", "d"), "鸟"),
                        ExampleWord("nurse", listOf("n", "ɜː", "s"), "护士"),
                        ExampleWord("girl", listOf("g", "ɜː", "l"), "女孩"),
                    ),
            ),
            Phoneme(
                id = "schwa",
                symbol = "ə",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "英语里最常见的弱读音。口型最放松，几乎不用力，声音短而轻。",
                examples =
                    listOf(
                        ExampleWord("about", listOf("ə", "b", "aʊ", "t"), "关于"),
                        ExampleWord("teacher", listOf("t", "iː", "tʃ", "ə"), "老师"),
                        ExampleWord("sofa", listOf("s", "əʊ", "f", "ə"), "沙发"),
                    ),
            ),
            Phoneme(
                id = "u_short",
                symbol = "ʌ",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "后元音短音。口型不大、张口自然，舌后部稍稍抬起，声音短促。",
                examples =
                    listOf(
                        ExampleWord("cup", listOf("k", "ʌ", "p"), "杯子"),
                        ExampleWord("bus", listOf("b", "ʌ", "s"), "公交车"),
                        ExampleWord("love", listOf("l", "ʌ", "v"), "爱"),
                    ),
            ),
            Phoneme(
                id = "u_long",
                symbol = "uː",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "后元音里的长音。嘴唇收圆并向前突出，舌后部抬到最高。",
                examples =
                    listOf(
                        ExampleWord("food", listOf("f", "uː", "d"), "食物"),
                        ExampleWord("shoe", listOf("ʃ", "uː"), "鞋"),
                        ExampleWord("blue", listOf("b", "l", "uː"), "蓝色"),
                    ),
            ),
            Phoneme(
                id = "u_short_round",
                symbol = "ʊ",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "uː 的短促版本。圆唇但不用力，舌位比 uː 略低、更松。",
                examples =
                    listOf(
                        ExampleWord("book", listOf("b", "ʊ", "k"), "书"),
                        ExampleWord("good", listOf("g", "ʊ", "d"), "好的"),
                        ExampleWord("put", listOf("p", "ʊ", "t"), "放"),
                    ),
            ),
            Phoneme(
                id = "aw_long",
                symbol = "ɔː",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "后元音长音。双唇收圆向前突出，舌后部抬起，声音拉长。",
                examples =
                    listOf(
                        ExampleWord("door", listOf("d", "ɔː"), "门"),
                        ExampleWord("ball", listOf("b", "ɔː", "l"), "球"),
                        ExampleWord("horse", listOf("h", "ɔː", "s"), "马"),
                    ),
            ),
            Phoneme(
                id = "o_short",
                symbol = "ɒ",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "英音特有的短音。双唇微圆、张口较大，舌后部稍抬，短促收尾。",
                examples =
                    listOf(
                        ExampleWord("hot", listOf("h", "ɒ", "t"), "热的"),
                        ExampleWord("clock", listOf("k", "l", "ɒ", "k"), "时钟"),
                        ExampleWord("box", listOf("b", "ɒ", "k", "s"), "盒子"),
                    ),
            ),
            Phoneme(
                id = "a_long",
                symbol = "ɑː",
                group = PhonemeGroup.MONOPHTHONG,
                tip = "后元音长音。口张到最大，舌尖离开下齿，舌身平躺向后缩。",
                examples =
                    listOf(
                        ExampleWord("car", listOf("k", "ɑː"), "汽车"),
                        ExampleWord("father", listOf("f", "ɑː", "ð", "ə"), "父亲"),
                        ExampleWord("park", listOf("p", "ɑː", "k"), "公园"),
                    ),
            ),
            // ---------- 元音 · 双元音 8 ----------
            Phoneme(
                id = "ei",
                symbol = "eɪ",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 e 向 ɪ 滑动。前重后轻，像一个音一口气滑过去。",
                examples =
                    listOf(
                        ExampleWord("day", listOf("d", "eɪ"), "白天"),
                        ExampleWord("name", listOf("n", "eɪ", "m"), "名字"),
                        ExampleWord("eight", listOf("eɪ", "t"), "八"),
                    ),
            ),
            Phoneme(
                id = "ai",
                symbol = "aɪ",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 a 向 ɪ 滑动。口型从大开快速收小。",
                examples =
                    listOf(
                        ExampleWord("my", listOf("m", "aɪ"), "我的"),
                        ExampleWord("time", listOf("t", "aɪ", "m"), "时间"),
                        ExampleWord("bike", listOf("b", "aɪ", "k"), "自行车"),
                    ),
            ),
            Phoneme(
                id = "oi",
                symbol = "ɔɪ",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 ɔ 向 ɪ 滑动。圆唇起、扁唇收，中间不要断开。",
                examples =
                    listOf(
                        ExampleWord("boy", listOf("b", "ɔɪ"), "男孩"),
                        ExampleWord("coin", listOf("k", "ɔɪ", "n"), "硬币"),
                        ExampleWord("voice", listOf("v", "ɔɪ", "s"), "嗓音"),
                    ),
            ),
            Phoneme(
                id = "ou",
                symbol = "əʊ",
                group = PhonemeGroup.DIPHTHONG,
                tip = "英音读作 əʊ：从松弛的 ə 滑向 ʊ，嘴唇逐渐收圆。",
                examples =
                    listOf(
                        ExampleWord("go", listOf("g", "əʊ"), "去"),
                        ExampleWord("home", listOf("h", "əʊ", "m"), "家"),
                        ExampleWord("boat", listOf("b", "əʊ", "t"), "小船"),
                    ),
            ),
            Phoneme(
                id = "au",
                symbol = "aʊ",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 a 向 ʊ 滑动。先张大嘴，再快速收圆双唇。",
                examples =
                    listOf(
                        ExampleWord("now", listOf("n", "aʊ"), "现在"),
                        ExampleWord("house", listOf("h", "aʊ", "s"), "房子"),
                        ExampleWord("mouth", listOf("m", "aʊ", "θ"), "嘴"),
                    ),
            ),
            Phoneme(
                id = "ie",
                symbol = "ɪə",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 ɪ 向 ə 滑动。前段清晰，后段放松、声音减轻。",
                examples =
                    listOf(
                        ExampleWord("ear", listOf("ɪə"), "耳朵"),
                        ExampleWord("here", listOf("h", "ɪə"), "这里"),
                        ExampleWord("beer", listOf("b", "ɪə"), "啤酒"),
                    ),
            ),
            Phoneme(
                id = "ea",
                symbol = "eə",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 e 向 ə 滑动。口型从半开逐渐放松成中性。",
                examples =
                    listOf(
                        ExampleWord("air", listOf("eə"), "空气"),
                        ExampleWord("chair", listOf("tʃ", "eə"), "椅子"),
                        ExampleWord("bear", listOf("b", "eə"), "熊"),
                    ),
            ),
            Phoneme(
                id = "ue",
                symbol = "ʊə",
                group = PhonemeGroup.DIPHTHONG,
                tip = "由 ʊ 向 ə 滑动。双唇先收圆再放松，现代英音常弱化。",
                examples =
                    listOf(
                        ExampleWord("tour", listOf("t", "ʊə"), "旅行"),
                        ExampleWord("pure", listOf("p", "j", "ʊə"), "纯净的"),
                        ExampleWord("tourist", listOf("t", "ʊə", "r", "ɪ", "s", "t"), "游客"),
                    ),
            ),
            // ---------- 辅音 · 爆破音 6 ----------
            Phoneme(
                id = "p",
                symbol = "p",
                group = PhonemeGroup.PLOSIVE,
                tip = "双唇爆破清音。双唇紧闭再突然放开送气，声带不振动。",
                examples =
                    listOf(
                        ExampleWord("pen", listOf("p", "e", "n"), "钢笔"),
                        ExampleWord("apple", listOf("æ", "p", "l"), "苹果"),
                        ExampleWord("cup", listOf("k", "ʌ", "p"), "杯子"),
                    ),
            ),
            Phoneme(
                id = "b",
                symbol = "b",
                group = PhonemeGroup.PLOSIVE,
                tip = "双唇爆破浊音。口型与 p 相同，但声带振动、送气更弱。",
                examples =
                    listOf(
                        ExampleWord("bag", listOf("b", "æ", "g"), "包"),
                        ExampleWord("baby", listOf("b", "eɪ", "b", "iː"), "婴儿"),
                        ExampleWord("job", listOf("dʒ", "ɒ", "b"), "工作"),
                    ),
            ),
            Phoneme(
                id = "t",
                symbol = "t",
                group = PhonemeGroup.PLOSIVE,
                tip = "舌尖齿龈爆破清音。舌尖抵上齿龈，突然放开送气。",
                examples =
                    listOf(
                        ExampleWord("tea", listOf("t", "iː"), "茶"),
                        ExampleWord("water", listOf("w", "ɔː", "t", "ə"), "水"),
                        ExampleWord("cat", listOf("k", "æ", "t"), "猫"),
                    ),
            ),
            Phoneme(
                id = "d",
                symbol = "d",
                group = PhonemeGroup.PLOSIVE,
                tip = "舌尖齿龈爆破浊音。位置同 t，但声带振动。",
                examples =
                    listOf(
                        ExampleWord("dog", listOf("d", "ɒ", "g"), "狗"),
                        ExampleWord("ladder", listOf("l", "æ", "d", "ə"), "梯子"),
                        ExampleWord("red", listOf("r", "e", "d"), "红色的"),
                    ),
            ),
            Phoneme(
                id = "k",
                symbol = "k",
                group = PhonemeGroup.PLOSIVE,
                tip = "舌根软腭爆破清音。舌后部抬起贴软腭，放开时送气。",
                examples =
                    listOf(
                        ExampleWord("key", listOf("k", "iː"), "钥匙"),
                        ExampleWord("school", listOf("s", "k", "uː", "l"), "学校"),
                        ExampleWord("book", listOf("b", "ʊ", "k"), "书"),
                    ),
            ),
            Phoneme(
                id = "g",
                symbol = "g",
                group = PhonemeGroup.PLOSIVE,
                tip = "舌根软腭爆破浊音。位置同 k，但声带振动。",
                examples =
                    listOf(
                        ExampleWord("go", listOf("g", "əʊ"), "去"),
                        ExampleWord("girl", listOf("g", "ɜː", "l"), "女孩"),
                        ExampleWord("bag", listOf("b", "æ", "g"), "包"),
                    ),
            ),
            // ---------- 辅音 · 摩擦音 10 ----------
            Phoneme(
                id = "f",
                symbol = "f",
                group = PhonemeGroup.FRICATIVE,
                tip = "唇齿摩擦清音。上齿轻触下唇，气流从缝隙摩擦而出。",
                examples =
                    listOf(
                        ExampleWord("five", listOf("f", "aɪ", "v"), "五"),
                        ExampleWord("coffee", listOf("k", "ɒ", "f", "iː"), "咖啡"),
                        ExampleWord("leaf", listOf("l", "iː", "f"), "叶子"),
                    ),
            ),
            Phoneme(
                id = "v",
                symbol = "v",
                group = PhonemeGroup.FRICATIVE,
                tip = "唇齿摩擦浊音。口型同 f，但要加上声带振动。",
                examples =
                    listOf(
                        ExampleWord("very", listOf("v", "e", "r", "iː"), "非常"),
                        ExampleWord("seven", listOf("s", "e", "v", "ə", "n"), "七"),
                        ExampleWord("five", listOf("f", "aɪ", "v"), "五"),
                    ),
            ),
            Phoneme(
                id = "th_voiceless",
                symbol = "θ",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿间摩擦清音。舌尖轻轻放在上下齿之间送气，不振动声带。",
                examples =
                    listOf(
                        ExampleWord("think", listOf("θ", "ɪ", "ŋ", "k"), "想"),
                        ExampleWord("bath", listOf("b", "ɑː", "θ"), "洗澡"),
                        ExampleWord("mouth", listOf("m", "aʊ", "θ"), "嘴"),
                    ),
            ),
            Phoneme(
                id = "th_voiced",
                symbol = "ð",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿间摩擦浊音。口型同 θ，但声带振动，听起来更柔和。",
                examples =
                    listOf(
                        ExampleWord("this", listOf("ð", "ɪ", "s"), "这个"),
                        ExampleWord("mother", listOf("m", "ʌ", "ð", "ə"), "母亲"),
                        ExampleWord("weather", listOf("w", "e", "ð", "ə"), "天气"),
                    ),
            ),
            Phoneme(
                id = "s",
                symbol = "s",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿龈摩擦清音。舌尖靠近齿龈，气流从窄缝挤出，像蛇嘶声。",
                examples =
                    listOf(
                        ExampleWord("sun", listOf("s", "ʌ", "n"), "太阳"),
                        ExampleWord("six", listOf("s", "ɪ", "k", "s"), "六"),
                        ExampleWord("bus", listOf("b", "ʌ", "s"), "公交车"),
                    ),
            ),
            Phoneme(
                id = "z",
                symbol = "z",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿龈摩擦浊音。位置同 s，但声带振动，像蜜蜂嗡嗡声。",
                examples =
                    listOf(
                        ExampleWord("zoo", listOf("z", "uː"), "动物园"),
                        ExampleWord("easy", listOf("iː", "z", "iː"), "容易的"),
                        ExampleWord("nose", listOf("n", "əʊ", "z"), "鼻子"),
                    ),
            ),
            Phoneme(
                id = "sh",
                symbol = "ʃ",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿龈后摩擦清音。嘴唇稍前突，舌面靠近硬腭，像让人安静时发出的嘘声。",
                examples =
                    listOf(
                        ExampleWord("she", listOf("ʃ", "iː"), "她"),
                        ExampleWord("fish", listOf("f", "ɪ", "ʃ"), "鱼"),
                        ExampleWord("sugar", listOf("ʃ", "ʊ", "g", "ə"), "糖"),
                    ),
            ),
            Phoneme(
                id = "zh",
                symbol = "ʒ",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿龈后摩擦浊音。口型同 ʃ，但声带振动，是英语中最少见的音。",
                examples =
                    listOf(
                        ExampleWord("vision", listOf("v", "ɪ", "ʒ", "ə", "n"), "视力"),
                        ExampleWord("measure", listOf("m", "e", "ʒ", "ə"), "测量"),
                        ExampleWord("usual", listOf("j", "uː", "ʒ", "uː", "ə", "l"), "通常的"),
                    ),
            ),
            Phoneme(
                id = "h",
                symbol = "h",
                group = PhonemeGroup.FRICATIVE,
                tip = "声门摩擦清音。口型随后面的元音变化，呼出一口轻气即可。",
                examples =
                    listOf(
                        ExampleWord("hi", listOf("h", "aɪ"), "嗨"),
                        ExampleWord("hand", listOf("h", "æ", "n", "d"), "手"),
                        ExampleWord("hotel", listOf("h", "əʊ", "t", "e", "l"), "酒店"),
                    ),
            ),
            Phoneme(
                id = "r",
                symbol = "r",
                group = PhonemeGroup.FRICATIVE,
                tip = "齿龈近音。舌尖卷起但不碰上齿龈，双唇略圆，英音 r 只在元音前发出。",
                examples =
                    listOf(
                        ExampleWord("red", listOf("r", "e", "d"), "红色的"),
                        ExampleWord("rain", listOf("r", "eɪ", "n"), "雨"),
                        ExampleWord("very", listOf("v", "e", "r", "iː"), "非常"),
                    ),
            ),
            // ---------- 辅音 · 破擦音 6 ----------
            Phoneme(
                id = "ch",
                symbol = "tʃ",
                group = PhonemeGroup.AFFRICATE,
                tip = "清辅音破擦音。舌尖抵齿龈后放开，先堵后摩擦，口型同 ʃ。",
                examples =
                    listOf(
                        ExampleWord("chair", listOf("tʃ", "eə"), "椅子"),
                        ExampleWord("teacher", listOf("t", "iː", "tʃ", "ə"), "老师"),
                        ExampleWord("watch", listOf("w", "ɒ", "tʃ"), "手表"),
                    ),
            ),
            Phoneme(
                id = "j_voiced",
                symbol = "dʒ",
                group = PhonemeGroup.AFFRICATE,
                tip = "浊辅音破擦音。口型同 tʃ，但声带振动，声音更重。",
                examples =
                    listOf(
                        ExampleWord("job", listOf("dʒ", "ɒ", "b"), "工作"),
                        ExampleWord("orange", listOf("ɒ", "r", "ɪ", "n", "dʒ"), "橙子"),
                        ExampleWord("bridge", listOf("b", "r", "ɪ", "dʒ"), "桥"),
                    ),
            ),
            Phoneme(
                id = "tr",
                symbol = "tr",
                group = PhonemeGroup.AFFRICATE,
                tip = "清辅音。t 与 r 连读成一体，舌尖先抵齿龈后放开、卷舌摩擦。",
                examples =
                    listOf(
                        ExampleWord("tree", listOf("tr", "iː"), "树"),
                        ExampleWord("train", listOf("tr", "eɪ", "n"), "火车"),
                        ExampleWord("street", listOf("s", "tr", "iː", "t"), "街道"),
                    ),
            ),
            Phoneme(
                id = "dr",
                symbol = "dr",
                group = PhonemeGroup.AFFRICATE,
                tip = "浊辅音。口型同 tr，但声带振动，听起来更沉。",
                examples =
                    listOf(
                        ExampleWord("dream", listOf("dr", "iː", "m"), "梦"),
                        ExampleWord("dress", listOf("dr", "e", "s"), "连衣裙"),
                        ExampleWord("children", listOf("tʃ", "ɪ", "l", "dr", "ə", "n"), "孩子们"),
                    ),
            ),
            Phoneme(
                id = "ts",
                symbol = "ts",
                group = PhonemeGroup.AFFRICATE,
                tip = "清辅音。t 与 s 快速连读，常见于名词复数和动词第三人称单数词尾。",
                examples =
                    listOf(
                        ExampleWord("cats", listOf("k", "æ", "ts"), "猫（复数）"),
                        ExampleWord("hats", listOf("h", "æ", "ts"), "帽子（复数）"),
                        ExampleWord("lots", listOf("l", "ɒ", "ts"), "许多"),
                    ),
            ),
            Phoneme(
                id = "dz",
                symbol = "dz",
                group = PhonemeGroup.AFFRICATE,
                tip = "浊辅音。口型同 ts，但声带振动，同样常见于词尾。",
                examples =
                    listOf(
                        ExampleWord("beds", listOf("b", "e", "dz"), "床（复数）"),
                        ExampleWord("hands", listOf("h", "æ", "n", "dz"), "手（复数）"),
                        ExampleWord("friends", listOf("f", "r", "e", "n", "dz"), "朋友（复数）"),
                    ),
            ),
            // ---------- 辅音 · 鼻音 3 ----------
            Phoneme(
                id = "m",
                symbol = "m",
                group = PhonemeGroup.NASAL,
                tip = "双唇鼻音。双唇紧闭，气流从鼻腔出来，声带振动。",
                examples =
                    listOf(
                        ExampleWord("man", listOf("m", "æ", "n"), "男人"),
                        ExampleWord("summer", listOf("s", "ʌ", "m", "ə"), "夏天"),
                        ExampleWord("time", listOf("t", "aɪ", "m"), "时间"),
                    ),
            ),
            Phoneme(
                id = "n",
                symbol = "n",
                group = PhonemeGroup.NASAL,
                tip = "齿龈鼻音。舌尖抵上齿龈，气流从鼻腔出，口型同 d。",
                examples =
                    listOf(
                        ExampleWord("nose", listOf("n", "əʊ", "z"), "鼻子"),
                        ExampleWord("sun", listOf("s", "ʌ", "n"), "太阳"),
                        ExampleWord("nine", listOf("n", "aɪ", "n"), "九"),
                    ),
            ),
            Phoneme(
                id = "ng",
                symbol = "ŋ",
                group = PhonemeGroup.NASAL,
                tip = "软腭鼻音。舌后部抵住软腭，气流从鼻腔出，不能发成 n 加 g。",
                examples =
                    listOf(
                        ExampleWord("sing", listOf("s", "ɪ", "ŋ"), "唱歌"),
                        ExampleWord("bank", listOf("b", "æ", "ŋ", "k"), "银行"),
                        ExampleWord("long", listOf("l", "ɒ", "ŋ"), "长的"),
                    ),
            ),
            // ---------- 辅音 · 舌侧音 1 ----------
            Phoneme(
                id = "l",
                symbol = "l",
                group = PhonemeGroup.LATERAL,
                tip = "齿龈舌侧音。舌尖抵上齿龈，气流从舌两侧流出。词尾的 l 舌后部要抬起。",
                examples =
                    listOf(
                        ExampleWord("leg", listOf("l", "e", "g"), "腿"),
                        ExampleWord("yellow", listOf("j", "e", "l", "əʊ"), "黄色的"),
                        ExampleWord("apple", listOf("æ", "p", "l"), "苹果"),
                    ),
            ),
            // ---------- 辅音 · 半元音 2 ----------
            Phoneme(
                id = "w",
                symbol = "w",
                group = PhonemeGroup.SEMIVOWEL,
                tip = "双唇半元音。双唇先收圆，再迅速滑向后面的元音。",
                examples =
                    listOf(
                        ExampleWord("we", listOf("w", "iː"), "我们"),
                        ExampleWord("window", listOf("w", "ɪ", "n", "d", "əʊ"), "窗户"),
                        ExampleWord("water", listOf("w", "ɔː", "t", "ə"), "水"),
                    ),
            ),
            Phoneme(
                id = "j",
                symbol = "j",
                group = PhonemeGroup.SEMIVOWEL,
                tip = "硬腭半元音。舌面抬向硬腭，双唇放松，像汉语拼音 y 的起始动作。",
                examples =
                    listOf(
                        ExampleWord("yes", listOf("j", "e", "s"), "是"),
                        ExampleWord("yellow", listOf("j", "e", "l", "əʊ"), "黄色的"),
                        ExampleWord("music", listOf("m", "j", "uː", "z", "ɪ", "k"), "音乐"),
                    ),
            ),
        )

    private val byId: Map<String, Phoneme> = all.associateBy { it.id }

    private val byCategory: Map<PhonemeCategory, List<Phoneme>> = all.groupBy { it.category }

    fun byId(id: String): Phoneme = requireNotNull(byId[id]) { "未知音标 id: $id" }

    fun findById(id: String): Phoneme? = byId[id]

    fun byCategory(category: PhonemeCategory): List<Phoneme> = byCategory.getValue(category)

    /** 按分组顺序返回 [category] 下的音标，供首页分行展示。 */
    fun groupedByCategory(category: PhonemeCategory): List<Pair<PhonemeGroup, List<Phoneme>>> =
        PhonemeGroup.of(category).mapNotNull { group ->
            all.filter { it.group == group }.takeIf { it.isNotEmpty() }?.let { group to it }
        }
}
