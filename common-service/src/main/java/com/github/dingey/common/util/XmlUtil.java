package com.github.dingey.common.util;

import com.github.dingey.common.exception.XmException;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class XmlUtil {
    private static volatile DocumentBuilderFactory factory;

    private XmlUtil() {
    }

    /**
     * 转换成xml格式
     *
     * @param t   示例
     * @param <T> 类型
     * @return xml
     */
    public static <T> String toXml(T t) {
        try {
            StringWriter sw = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(t.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(t, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new XmException(e);
        }
    }

    /**
     * 解析xml转换为目标实例
     *
     * @param target 实例类
     * @param <T>    类型
     * @return 目标实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseXml(String xml, Class<T> target) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(target);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(new StringReader(xml)));
            return (T) unmarshaller.unmarshal(xmlSource);
        } catch (Exception e) {
            throw new XmException(e);
        }
    }

    /**
     * 一维的xml格式转换成map
     *
     * @param xml xml数据
     * @return map
     */
    public static Map<String, String> parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = safeFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            Element element = parse.getDocumentElement();
            if (parse.hasChildNodes()) {
                Map<String, String> m = new LinkedHashMap<>();
                NodeList nodes = element.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        m.put(node.getNodeName(), node.getTextContent());
                    }
                }
                return m;
            } else {
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * map转换成xml格式
     *
     * @param data 参数
     * @return xml
     */
    public static String toXml(Map<?, ?> data) {
        return toXml(data, "root");
    }

    /**
     * map转换成xml格式
     *
     * @param data     参数
     * @param rootName 跟名称
     * @return xml
     */
    public static String toXml(Map<?, ?> data, String rootName) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document document = documentBuilder.newDocument();
            org.w3c.dom.Element root = document.createElement(StringUtils.hasText(rootName) ? rootName : "xml");
            document.appendChild(root);
            for (Object key : data.keySet()) {
                Object value = data.get(key);
                if (value == null) {
                    value = "";
                }
                org.w3c.dom.Element filed = document.createElement(String.valueOf(key));
                filed.appendChild(document.createTextNode(String.valueOf(value)));
                root.appendChild(filed);
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            String output = writer.getBuffer().toString();
            writer.close();
            return output;
        } catch (Exception e) {
            throw new XmException(e);
        }
    }

    private static DocumentBuilderFactory safeFactory() {
        if (factory == null) {
            synchronized (XmlUtil.class) {
                if (factory == null) {
                    try {
                        factory = DocumentBuilderFactory.newInstance();
                        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                        factory.setXIncludeAware(false);
                        factory.setExpandEntityReferences(false);
                    } catch (Exception e) {
                        throw new XmException(e);
                    }
                }
            }
        }
        return factory;
    }

    public static void main(String[] args) {
        Map<String, String> map = parseXml("<xml><a>aaa</a><b>b2</b><list><i>001</i><i>002</i></list></xml>");
        System.out.println(map);

        System.out.println(toXml(map, ""));
    }
}
