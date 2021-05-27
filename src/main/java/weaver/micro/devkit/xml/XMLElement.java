package weaver.micro.devkit.xml;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.print.MinimumType;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 仅支持构造简单的XML文本, 不支持解析<br>
 * 已知问题: 如果children集合中有对自己的引用, 会导致toString()时栈溢出
 *
 * <hr/>
 * Non-thread safe.
 *
 * @author ruan4261
 */
@MinimumType
public class XMLElement implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private static final transient String LINE_SEPARATOR = "\n";
    private boolean printHead;
    private String encoding;
    private String name;
    private String content;
    private List<XMLElement> children;
    private Map<String, String> attributes;

    {
        this.children = new ArrayList<XMLElement>();
        this.attributes = new HashMap<String, String>(8);
        this.encoding = Charset.defaultCharset().name();
        this.printHead = true;
    }

    public XMLElement(String name) {
        xmlNameVerify(name);
        this.name = name;
    }

    public boolean isPrintHead() {
        return printHead;
    }

    public void setPrintHead(boolean printHead) {
        this.printHead = printHead;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        Assert.notEmpty(encoding);
        this.encoding = encoding;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        xmlNameVerify(name);
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = xmlEscape(content);
    }

    public XMLElement addChild(String name) {
        XMLElement ele = new XMLElement(name);
        this.children.add(ele);
        return ele;
    }

    public void addChild(XMLElement ele) {
        Assert.notNull(ele);
        this.children.add(ele);
    }

    public void addAttribute(String key, String value) {
        xmlNameVerify(key);
        value = xmlEscape(value);
        this.attributes.put(key, value);
    }

    public List<XMLElement> getChildren() {
        return this.children;
    }

    public List<XMLElement> copyChildren() {
        List<XMLElement> childrenCopy = new ArrayList<XMLElement>(this.children.size());
        for (XMLElement ele : this.children) {
            childrenCopy.add(ele.clone());
        }
        return childrenCopy;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public Map<String, String> copyAttributes() {
        return new HashMap<String, String>(this.attributes);
    }

    public void clearAttributes() {
        this.attributes = new HashMap<String, String>();
    }

    public void clearChildren() {
        this.children = new ArrayList<XMLElement>();
    }

    @Override
    public String toString() {
        // this element is xml root
        int baseLength = (this.name.length() << 1) + 4 + (this.printHead ? this.encoding.length() + 32 : 0);
        int extendLength = (this.attributes.size() << 3) + (this.children.size() << 4) + baseLength;
        StringBuilder builder = new StringBuilder(extendLength);
        if (this.printHead)
            builder.append("<?xml version=\"1.0\" encoding=\"")
                    .append(this.encoding)
                    .append("\"?>")
                    .append(LINE_SEPARATOR);

        this.buildXmlString(builder, "");
        return builder.toString();
    }

    protected void buildXmlString(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix);
        builder.append("<")
                .append(this.name);
        for (Map.Entry<String, String> entry : this.attributes.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();

            if (k != null && v != null)
                builder.append(" ")
                        .append(k)
                        .append("=")
                        .append("\"")
                        .append(v)
                        .append("\"");
        }
        builder.append(">");

        if (this.content != null) {
            builder.append(LINE_SEPARATOR)
                    .append(linePrefix)
                    .append("\t")
                    .append(this.content)
                    .append(LINE_SEPARATOR)
                    .append(linePrefix);
        }

        if (!children.isEmpty()) {
            if (this.content == null)
                builder.append(LINE_SEPARATOR);

            for (XMLElement ele : this.children) {
                ele.buildXmlString(builder, linePrefix + "\t");
                builder.append(LINE_SEPARATOR);
            }
            builder.append(linePrefix);
        }

        builder.append("</")
                .append(this.name)
                .append(">");
    }

    @Override
    @SuppressWarnings("all")
    public XMLElement clone() {
        XMLElement ele = new XMLElement(this.name);
        ele.attributes = new HashMap<String, String>(this.attributes);
        for (XMLElement child : this.children) {
            ele.addChild(child.clone());
        }
        return ele;
    }

    /*
    #Token Rules
    ## Tag name
    Allow: 0~9 Aa~Zz - _ . :

    ## Attribute name
    Allow: 0~9 Aa~Zz - _ . :

    ## Value
    Need transfer:
    < -> &lt;
    > -> &gt;
    ' -> &apos;
    " -> &quot;
    & -> &amp;
    严格地讲，在 XML 中仅有字符 "<"和"&" 是非法的。省略号、引号和大于号是合法的，但是把它们替换为实体引用是个好的习惯。
     */

    static final List<String> token_list = Collections.unmodifiableList(Arrays.asList(
            "lt",
            "gt",
            "apos",
            "quot",
            "amp"
    ));

    /**
     * 文本内容转义
     */
    static String xmlEscape(String str) {
        Assert.notNull(str);
        StringBuilder builder = new StringBuilder(str);

        for (int i = 0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            switch (ch) {
                case '&': {
                    boolean hasSemicolon = false;
                    int begin = i + 1;
                    int limit = Math.min(builder.length(), i + 6);
                    int end = begin;
                    for (; end < limit; end++) {
                        char ch2 = builder.charAt(end);
                        if (ch2 == ';') {
                            hasSemicolon = true;
                            break;
                        }
                    }

                    if (hasSemicolon) {
                        String mean = builder.substring(begin, end);
                        if (token_list.contains(mean)) {
                            // 已转义
                            i = end;
                            continue;
                        }
                    }

                    // 将其转义
                    builder.insert(begin, "amp;");
                    i += 4;
                }
                break;
                case '<': {
                    builder.replace(i, i + 1, "&lt;");
                    i += 3;
                }
                break;
                case '>': {
                    builder.replace(i, i + 1, "&gt;");
                    i += 3;
                }
                break;
                case '\'': {
                    builder.replace(i, i + 1, "&apos;");
                    i += 5;
                }
                break;
                case '\"': {
                    builder.replace(i, i + 1, "&quot;");
                    i += 5;
                }
                break;
            }
        }

        return builder.toString();
    }

    /**
     * 校验tag name或attribute name
     */
    static void xmlNameVerify(String str) {
        Assert.notEmpty(str);
        int len = str.length();
        char first = str.charAt(0);
        if (first != '_'
                && (first < 'a' || first > 'z')
                && (first < 'A' || first > 'Z'))
            throw new IllegalArgumentException("Illegal xml sign: " + str);

        for (int i = 1; i < len; i++) {
            char ch = str.charAt(i);
            if ((ch < '0' || ch > '9')
                    && (ch < 'a' || ch > 'z')
                    && (ch < 'A' || ch > 'Z')
                    && ch != '-' && ch != '_' && ch != '.' && ch != ':')
                throw new IllegalArgumentException("Illegal xml sign: " + str);
        }
    }

}