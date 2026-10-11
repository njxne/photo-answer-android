package com.photoanswer;

/** 内置提示词。所有提示词按「年级 → 学段」自动分支。 */
public final class Prompt {

    public static final String[] GRADES = {
            "一年级", "二年级", "三年级", "四年级", "五年级", "六年级",
            "初一", "初二", "初三",
            "高一", "高二", "高三"
    };

    public static final String[] SUBJECTS = {
            "自动识别", "数学", "语文", "英语", "物理", "化学",
            "生物", "历史", "地理", "政治", "科学"
    };

    private Prompt() {
    }

    public static int gradeIndex(String grade) {
        for (int i = 0; i < GRADES.length; i++) {
            if (GRADES[i].equals(grade)) return i;
        }
        return 3;
    }

    /** 学段：小学 / 初中 / 高中。 */
    public static String stage(String grade) {
        int i = gradeIndex(grade);
        if (i < 6) return "小学";
        if (i < 9) return "初中";
        return "高中";
    }

    /** 该学段的作文字数要求。 */
    public static String wordCount(String grade) {
        int i = gradeIndex(grade);
        if (i < 2) return "150~200 字";
        if (i < 4) return "300~400 字";
        if (i < 6) return "400~500 字";
        if (i < 9) return "600~700 字";
        return "800~1000 字";
    }

    public static final String MARK_PREFIX = "#第";

    /**
     * 拍题作答：直接给标准答案，覆盖全部题型。
     *
     * 每道题的答案必须单独以一行「#第N题」开头，客户端按这个标记切成多题显示，
     * 切题时不会重新请求模型。
     */
    public static String solve(String grade, String subject, int imgCount) {
        String sub = "自动识别".equals(subject) ? "（按图片内容自动判断）" : subject;
        StringBuilder b = new StringBuilder();
        b.append("你是一名中国").append(grade).append("老师，现在给学生的作业提供标准答案。\n\n");
        b.append("【图片】共 ").append(imgCount)
                .append(" 张，按顺序是同一份作业的连续页面。\n");
        b.append("【学科】").append(sub).append("\n");
        b.append("【学段】").append(stage(grade)).append("\n\n");
        b.append("【题型覆盖】必须正确处理下面这些题型：\n");
        b.append("· 数学：口算、直接写得数、竖式计算、递等式（脱式）计算、简便运算、混合运算、估算、")
                .append("比较大小、求未知数、解方程、解比例、列式计算、文字题；\n");
        b.append("· 数学：填空、判断、选择、单位换算、分数小数百分数互化、比和比例、化简比、求比值、")
                .append("找规律、统计图表、可能性；\n");
        b.append("· 数学：应用题（行程、工程、盈亏、植树、鸡兔同笼、和差倍、年龄、分数百分数应用、")
                .append("归一归总、单价总价、折扣利率税率）；\n");
        b.append("· 数学：图形与几何（周长、面积、表面积、体积、角度、三角形与圆的图形计算）；\n");
        b.append("· 语文：看拼音写词语、组词、多音字、近义词反义词、词语解释、造句、选词填空、")
                .append("修改病句、句式转换、关联词、标点符号、修辞判断、古诗文默写、课文填空、阅读理解；\n");
        b.append("· 英语：单词拼写、英汉互译、词组翻译、单项选择、选词填空、句型转换、连词成句、")
                .append("按要求写出词的正确形式、阅读理解；\n");
        b.append("· 理化生：实验器材与步骤、单位换算、公式代入计算、判断与选择，")
                .append("计算题写出「已知 / 公式 / 代入 / 结果」四步；\n");
        b.append("· 政史地：填空、判断、选择、材料分析，直接给标准答案要点。\n\n");
        b.append("【输出格式】（最重要，必须严格遵守，否则无法解析）：\n");
        b.append("每道题的答案前面单独占一行标记：")
                .append(MARK_PREFIX).append("N题（N 是阿拉伯数字题号，从 1 开始）\n");
        b.append("示例：\n");
        b.append(MARK_PREFIX).append("1题\n");
        b.append("  125+36\n");
        b.append("= 161\n");
        b.append("答：和是 161。\n");
        b.append(MARK_PREFIX).append("2题\n");
        b.append("答：这个数是 24。\n\n");
        b.append("【输出规则】\n");
        b.append("1. 每题都必须有 ").append(MARK_PREFIX)
                .append("N题 这一行，题号连续，不要漏题、不要重复。\n");
        b.append("2. 一道题里有若干小问（如 1. … 2. …）时，整体算作这一题，小问写在同一题内。\n");
        b.append("3. 不要复述题目原文；禁止出现「解析」「思路」「知识点」「考点」「方法」")
                .append("「点评」「小结」「本题考查」这类讲解。\n");
        b.append("4. 数学类计算题必须用递等式（脱式）计算：每一步单独一行，等号写在行首，")
                .append("最后写一行「答：……」\n");
        b.append("5. 填空、判断、选择类直接给答案：填空题写要填的内容，判断题写「√」或「×」，")
                .append("选择题写选项字母（必要时补上内容），不要写讲解。\n");
        b.append("6. 应用题写「算式 + 答」，几何题写「公式代入 + 结果 + 答」，")
                .append("结论统一以「答：」开头并带单位。\n");
        b.append("7. 语文、英语题直接给标准答案内容（词语、句子、译文、单词、选项等）。\n");
        b.append("8. 必须符合").append(grade).append("的教学标准，只用该年级学过的方法和写法，不超纲。\n");
        b.append("9. 只输出答案本身，不要使用 Markdown 标记（不要 ** 号，也不要用「第N题」")
                .append("这种文字行代替 ").append(MARK_PREFIX).append("N题 标记）。\n");
        b.append("10. 图片模糊或看不清题目时，只回复：图片不清晰，请重新拍摄。\n");
        return b.toString();
    }

    /** 批改作文。 */
    public static String gradeEssay(String grade) {
        StringBuilder b = new StringBuilder();
        b.append("你是一名中国").append(grade).append("语文老师，请批改下面这篇作文")
                .append("（可能是图片，也可能是文字）。\n\n");
        b.append("严格按下面的格式输出，不要有任何多余内容：\n\n");
        b.append("【得分】__分（满分 100）\n");
        b.append("【等级】优 / 良 / 合格 / 待提高\n");
        b.append("【优点】\n1. …\n2. …\n");
        b.append("【问题】\n1. …\n2. …\n");
        b.append("【逐段修改建议】\n第 1 段：…\n第 2 段：…\n");
        b.append("【修改后全文】\n（直接给出修改完成的完整作文）\n\n");
        b.append("要求：评分和修改都要符合").append(grade).append("的写作水平标准，")
                .append("语言贴近该年级学生，不要拔高成成人文章；字数要求 ")
                .append(wordCount(grade)).append("。\n");
        b.append("不要使用 Markdown 标记。\n");
        return b.toString();
    }

    /** AI 生作文。 */
    public static String writeEssay(String grade, String topic) {
        StringBuilder b = new StringBuilder();
        b.append("你是一名中国").append(grade).append("语文老师，请根据下面的要求写一篇")
                .append(grade).append("水平的作文。\n\n");
        b.append("【写作要求】\n").append(topic.trim()).append("\n\n");
        b.append("【输出规则】\n");
        b.append("1. 直接输出作文正文，不要任何说明、点评或多余文字。\n");
        b.append("2. 字数 ").append(wordCount(grade)).append("，符合").append(grade)
                .append("的要求。\n");
        b.append("3. 语言符合该年级学生的真实写作水平，自然朴实，不堆砌辞藻。\n");
        b.append("4. 结构完整：开头点题、主体具体、结尾升华。\n");
        b.append("5. 不要使用 Markdown 标记。\n");
        return b.toString();
    }
}
