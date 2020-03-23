package com.billin.www.commondmodual.utils.multilanguage;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * 写入多语言文件
 * todo 添加日志功能；添加删除节点功能
 * <p>
 * Created by billin on 2018/3/22.
 */
public class MultiLanguageWriter {

    private static final String TAG_STRING = "string";

    private static final String ATTR_NAME = "name";

    private static final String TAG_RESOURCES = "resources";

    private Scanner mScanner = new Scanner(System.in);

    private List<String> mExtraPath = new ArrayList<>();

    /**
     * 在 path 目录下添加字符串资源，如果检查到 path 目录或者 extraPath 目录下面有相同的字符串将会跳过这个
     * 字符串的插入。跳过的字符串将会打印在控制台当中。
     */
    public void run(String inputFilePath, String outputPath) {

        // 读取需要添加多语言文件
        File input = new File(inputFilePath);
        try {
            if (!input.exists()) {
                throw new RuntimeException("input file not exist " + input);
            }

            BufferedReader reader = new BufferedReader(new FileReader(input));

            String s;

            // 正在处理的语言
            String language = null;

            // 输出文档
            File output = null;
            Document outputDocument = null;

            // 读取判断文档
            Map<String, Document> extraCheckDocuments = new HashMap<>();

            // 循环读取 input file 的每一行内容
            while ((s = reader.readLine()) != null) {

                // 去除空行
                if (s.trim().isEmpty()) continue;

                if (isLanguageTag(s)) {

                    // write the content into xml file
                    if (outputDocument != null) {
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(outputDocument);
                        StreamResult result = new StreamResult(output);
                        transformer.transform(source, result);
                    }

                    // config output language file
                    language = getLanguageTag(s);
                    output = getLanguageFile(language, outputPath);
                    outputDocument = readFile(output);

                    // config check language files
                    extraCheckDocuments.clear();
                    extraCheckDocuments.put(outputPath, outputDocument);
                    for (String checkPath : mExtraPath) {
                        File file = getLanguageFileWithoutCreate(getLanguageTag(s), checkPath);
                        if (file == null) continue;

                        extraCheckDocuments.put(checkPath, readFile(file));
                    }
                } else {
                    if (outputDocument == null)
                        throw new RuntimeException("input file is invalidate");

                    // add or replace new node to language file
                    Element node = parseElement(outputDocument, s);

                    // find attr
                    Set<String> conflictRes = new HashSet<>();
                    for (String checkPath : extraCheckDocuments.keySet()) {
                        Element checkNode = getElementWithAttrName(extraCheckDocuments.get(checkPath), node.getAttribute(ATTR_NAME));

                        if (checkNode == null) continue;

                        conflictRes.add(checkPath);
                    }

                    // 如果 name 属性的 value 不存在直接添加就好了
                    if (conflictRes.isEmpty()) {
                        outputDocument.getElementsByTagName(TAG_RESOURCES).item(0).appendChild(node);
                        continue;
                    }

                    // 如果 name 属性的 value 存在，分为两种情况处理
                    // 1. 如果对应的 value 相同且仅位于 outputPath 目录下那么跳过且不打印到控制台当中
                    // 2. 其余情况为了安全也一律跳过并打印到控制台当中
                    if (conflictRes.size() > 1 || !conflictRes.contains(outputPath)) {
                        // confident 2: print log to console

                        for (String key : conflictRes) {
                            System.out.println(String.format(Locale.US,
                                    "conflict in PATH %s LANGUAGE %s NAME %s",
                                    key, language, node.getAttribute(ATTR_NAME)));
                        }
                    }
                }
            }

            // 写入最后的文件
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(outputDocument);
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);

            reader.close();
        } catch (IOException |
                SAXException |
                ParserConfigurationException |
                TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在添加前检查 rootPath 是否存在相同字符串，如果存在相同字符串那么将会跳过这个字符串的插入
     */
    public void addExtraCheckRootDir(String rooPath) {
        mExtraPath.add(rooPath);
    }

    public void addExtraCheckRootDir(List<String> rootPaths) {
        mExtraPath.addAll(rootPaths);
    }

    /**
     * 判断输入文件中的某一行是否是语言开始表示
     */
    private boolean isLanguageTag(String line) {
        return line.contains("------------------------");
    }

    /**
     * 获取某一行语言标记的语言
     */
    private String getLanguageTag(String line) {
        return line.replace('-', ' ').trim();
    }

    /**
     * 读取一个文件的内容到 StringBuilder 中
     */
    private Document readFile(File file)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc;
        try {
            doc = docBuilder.parse(file);
        } catch (SAXParseException e) {
            System.out.println("parse xml error, recreate a new xml file: " + e);
            doc = docBuilder.newDocument();

            // add root element
            Element rootElement = doc.createElement(TAG_RESOURCES);
            doc.appendChild(rootElement);
        }

        return doc;
    }

    /**
     * 寻找对应语言的文件，如果该语言文件不存在则自动创建
     */
    private File getLanguageFile(String languageName, String outputDirPath) throws IOException {
        Language language = Language.get(languageName);
        if (language == null) {
            throw new RuntimeException("cannot find " + languageName + " language in Language");
        }

        File languageDir = new File(outputDirPath + language.getDirName());
        if (!languageDir.exists() || !languageDir.isDirectory()) {
            if (!languageDir.mkdirs())
                throw new RuntimeException("cannot create dir: " + languageDir);
        }

        File languageFile = new File(languageDir, "strings.xml");
        if (!languageFile.exists()) {
            if (!languageFile.createNewFile())
                throw new RuntimeException("cannot create file: " + languageFile);
        }

        return languageFile;
    }

    /**
     * 寻找对应的语言文件，如果该语言文件不存在则返回 null
     */
    @Nullable
    private File getLanguageFileWithoutCreate(String languageName, String outputDirPath) throws IOException {
        Language language = Language.get(languageName);
        if (language == null) {
            throw new RuntimeException("cannot find " + languageName + " language in Language");
        }

        File languageDir = new File(outputDirPath + language.getDirName());
        if (!languageDir.exists() || !languageDir.isDirectory()) {
            return null;
        }

        File languageFile = new File(languageDir, "strings.xml");
        if (!languageFile.exists()) {
            return null;
        }

        return languageFile;
    }

    private Element getElementWithAttrName(Document document, String name) {
        NodeList nodeList = document.getElementsByTagName(TAG_STRING);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);

            if (node.getAttribute(ATTR_NAME).equals(name)) {
                return node;
            }
        }

        return null;
    }

    private Element parseElement(Document document, String s) {
        Pattern pattern = Pattern.compile("name=\"(.*?)\"");
        Matcher matcher = pattern.matcher(s);

        // 查找字符串 key
        String attrValue = null;
        if (matcher.find()) {
            attrValue = matcher.group(1);
        }

        if (attrValue == null) throw new RuntimeException("attrValue not find: " + s);

        // 查找字符串 value
        String value = null;
        pattern = Pattern.compile(">(.*)</string>$");
        matcher = pattern.matcher(s);
        if (matcher.find()) {
            value = matcher.group(1);
        }

        if (value == null) throw new RuntimeException("value not find: " + s);

        Element node = document.createElement(TAG_STRING);
        Attr nameAttr = document.createAttribute(ATTR_NAME);
        nameAttr.setValue(attrValue);
        node.setAttributeNode(nameAttr);
        node.appendChild(document.createTextNode(value));

        return node;
    }
}
