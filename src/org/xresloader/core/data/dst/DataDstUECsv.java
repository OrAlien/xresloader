package org.xresloader.core.data.dst;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.xresloader.core.ProgramOptions;
import org.xresloader.core.data.dst.DataDstWriterNode.DataDstChildrenNode;
import org.xresloader.core.data.dst.DataDstWriterNode.DataDstFieldDescriptor;
import org.xresloader.core.data.dst.DataDstWriterNode.DataDstMessageDescriptor;
import org.xresloader.core.data.dst.DataDstWriterNode.JAVA_TYPE;
import org.xresloader.core.data.err.ConvException;
import org.xresloader.core.data.src.DataSrcImpl;
import org.xresloader.core.scheme.SchemeConf;

/**
 * Created by owentou on 2019/04/08.
 */
public class DataDstUECsv extends DataDstUEBase {
    /**
     * @return 协议处理器名字
     */
    public String name() {
        return "ue csv";
    }

    public void appendCommonHeader(CSVPrinter sp) throws IOException {
        sp.printComment("This file is generated by xresloader, please don't edit it.");
    }

    private class UEBuildObject {
        StringBuffer sb = null;
        CSVPrinter csv = null;
        ArrayList<HashMap.Entry<String, DataDstFieldDescriptor>> paddingFields = null;
    }

    @Override
    protected boolean isRecursiveEnabled() {
        return SchemeConf.getInstance().getUEOptions().enableRecursiveMode;
    }

    @Override
    protected Object buildForUEOnInit() throws IOException {
        UEBuildObject ret = new UEBuildObject();
        ret.sb = new StringBuffer();
        ret.csv = new CSVPrinter(ret.sb, CSVFormat.INFORMIX_UNLOAD_CSV.withQuoteMode(QuoteMode.ALL));

        appendCommonHeader(ret.csv);
        ret.csv.printComment(String.format("%s=%s", "xres_ver", ProgramOptions.getInstance().getVersion()));
        ret.csv.printComment(String.format("%s=%s", "data_ver", ProgramOptions.getInstance().getDataVersion()));
        ret.csv.printComment(String.format("%s=%d", "count", DataSrcImpl.getOurInstance().getRecordNumber()));
        ret.csv.printComment(String.format("%s=%s", "hash_code", "no hash code"));

        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected byte[] buildForUEOnFinal(Object buildObj) {
        // 带编码的输出
        String encoding = SchemeConf.getInstance().getKey().getEncoding();
        if (null == encoding || encoding.isEmpty())
            return ((UEBuildObject) buildObj).sb.toString().getBytes();

        return ((UEBuildObject) buildObj).sb.toString().getBytes(Charset.forName(encoding));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void buildForUEOnPrintHeader(Object buildObj, ArrayList<Object> rowData, UEDataRowRule rule,
            UECodeInfo codeInfo) throws IOException {
        if (!isRecursiveEnabled()) {
            ((UEBuildObject) buildObj).csv.printRecord(rowData);
        } else {
            HashSet<String> dumpedFields = new HashSet<String>();
            ArrayList<String> finalRowData = new ArrayList<String>();
            ArrayList<HashMap.Entry<String, DataDstFieldDescriptor>> paddingFields = new ArrayList<HashMap.Entry<String, DataDstFieldDescriptor>>();
            ((UEBuildObject) buildObj).paddingFields = paddingFields;
            finalRowData.ensureCapacity(codeInfo.desc.getTypeDescriptor().fields.size() + 1); // 1 for additional Name
                                                                                              // field
            paddingFields.ensureCapacity(codeInfo.desc.getTypeDescriptor().fields.size());
            for (Object keyName : rowData) {
                dumpedFields.add(keyName.toString());
                finalRowData.add(keyName.toString());
            }

            for (HashMap.Entry<String, DataDstFieldDescriptor> varPair : codeInfo.desc.getTypeDescriptor().fields
                    .entrySet()) {
                String varName = getIdentName(varPair.getKey());
                if (dumpedFields.contains(varName)) {
                    continue;
                }

                paddingFields.add(varPair);
                finalRowData.add(varName);
            }

            ((UEBuildObject) buildObj).csv.printRecord(finalRowData);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void buildForUEOnPrintRecord(Object buildObj, ArrayList<Object> rowData, UEDataRowRule rule,
            UECodeInfo codeInfo) throws IOException {
        if (!isRecursiveEnabled()) {
            ((UEBuildObject) buildObj).csv.printRecord(rowData);
        } else {
            ArrayList<String> finalRowData = new ArrayList<String>();
            finalRowData.ensureCapacity(rowData.size() + ((UEBuildObject) buildObj).paddingFields.size());

            for (Object val : rowData) {
                if (val == null) {
                    finalRowData.add("");
                } else {
                    finalRowData.add(val.toString());
                }
            }

            for (HashMap.Entry<String, DataDstFieldDescriptor> varPair : ((UEBuildObject) buildObj).paddingFields) {
                StringBuffer sb = new StringBuffer();
                pickValueFieldCsvDefaultImpl(sb, varPair.getValue());
                finalRowData.add(sb.toString());
            }

            ((UEBuildObject) buildObj).csv.printRecord(finalRowData);
        }
    }

    @Override
    public DataDstWriterNode compile() {
        ProgramOptions.getLoger().error("UE-CSV can not be protocol description.");
        return null;
    }

    @SuppressWarnings("unchecked")
    private void writeConstData(CSVPrinter sp, Object data, String prefix) throws IOException {
        // null
        if (null == data) {
            sp.printRecord(prefix, "");
            return;
        }

        // 数字
        // 枚举值已被转为Java Long，会在这里执行
        if (data instanceof Number) {
            sp.printRecord(prefix, data);
            return;
        }

        // 布尔
        if (data instanceof Boolean) {
            sp.printRecord(prefix, ((Boolean) data) ? 1 : 0);
            return;
        }

        // 字符串&二进制
        if (data instanceof String) {
            sp.printRecord(prefix, data);
            return;
        }

        // 列表
        if (data instanceof List) {
            List<Object> ls = (List<Object>) data;
            for (int i = 0; i < ls.size(); ++i) {
                if (prefix.isEmpty()) {
                    writeConstData(sp, ls.get(i), String.format("%d", i));
                } else {
                    writeConstData(sp, ls.get(i), String.format("%s.%d", prefix, i));
                }
            }
            return;
        }

        // Hashmap
        if (data instanceof Map) {
            Map<String, Object> mp = (Map<String, Object>) data;
            for (Map.Entry<String, Object> item : mp.entrySet()) {
                if (prefix.isEmpty()) {
                    writeConstData(sp, item.getValue(), String.format("%s", item.getKey()));
                } else {
                    writeConstData(sp, item.getValue(), String.format("%s.%s", prefix, item.getKey()));
                }
            }
            return;
        }

        sp.printRecord(prefix, data.toString());
    }

    /**
     * 和输出格式无关的常量转储功能
     * 
     * @param data 常量数据集
     * @return 常量代码
     */
    @Override
    public String dumpConstForUE(HashMap<String, Object> data, UEDataRowRule rule) throws IOException {
        StringBuffer sb = new StringBuffer();
        CSVPrinter csv = new CSVPrinter(sb, CSVFormat.EXCEL.withHeader(getIdentName("Name"), getIdentName("Value")));

        appendCommonHeader(csv);
        writeConstData(csv, data, "");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    final protected Object pickValueField(Object buildObj, DataDstWriterNodeWrapper desc) throws ConvException {
        if (!isRecursiveEnabled()) {
            Object ret = pickValueFieldBaseImpl(desc, 0);
            if (ret == null) {
                switch (desc.getJavaType()) {
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE: {
                    ret = "0";
                    break;
                }
                case BOOLEAN: {
                    ret = "False";
                    break;
                }
                default: {
                    ret = "";
                    break;
                }
                }
            }

            return ret;
        }

        StringBuffer fieldSB = new StringBuffer();
        pickValueFieldCsvImpl(fieldSB, desc);
        String ret = fieldSB.toString();
        // empty list to nothing
        if (ret.equalsIgnoreCase("()")) {
            ret = "";
        }
        return ret;
    }

    protected void pickValueFieldCsvImpl(StringBuffer fieldSB, DataDstWriterNodeWrapper descWrapper)
            throws ConvException {
        if (null == descWrapper || null == descWrapper.descs || descWrapper.descs.isEmpty()) {
            return;
        }

        DataDstWriterNode desc = descWrapper.GetWriterNode(0);
        if (desc == null) {
            return;
        }

        if (descWrapper.isList) {
            if (descWrapper.descs.isEmpty()) {
                return;
            }

            fieldSB.append("(");
            boolean hasListData = false;
            for (int i = 0; i < descWrapper.descs.size(); ++i) {
                if (hasListData) {
                    fieldSB.append(",");
                }
                if (pickValueFieldCsvImpl(fieldSB, descWrapper.descs.get(i))) {
                    hasListData = true;
                } else {
                    if (hasListData) {
                        fieldSB.deleteCharAt(fieldSB.length() - 1);
                    }
                }
            }
            fieldSB.append(")");
        } else {
            pickValueFieldCsvImpl(fieldSB, desc);
        }
    }

    protected boolean pickValueFieldCsvImpl(StringBuffer fieldSB, DataDstWriterNode desc) throws ConvException {
        if (desc.getType() == JAVA_TYPE.MESSAGE) {
            fieldSB.append("(");
            HashSet<String> dumpedFields = new HashSet<String>();

            boolean isFirst = true;
            for (HashMap.Entry<String, DataDstChildrenNode> child : desc.getChildren().entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    fieldSB.append(",");
                }
                String varName = getIdentName(child.getKey());
                dumpedFields.add(varName);
                fieldSB.append(varName);
                fieldSB.append("=");

                boolean isString = false;
                if (child.getValue().innerDesc != null) {
                    isString = child.getValue().innerDesc.getType() == JAVA_TYPE.STRING
                            || child.getValue().innerDesc.getType() == JAVA_TYPE.BYTES;
                } else if (!child.getValue().nodes.isEmpty()) {
                    isString = child.getValue().nodes.get(0).getType() == JAVA_TYPE.STRING
                            || child.getValue().nodes.get(0).getType() == JAVA_TYPE.BYTES;
                }

                if (child.getValue().innerDesc.isList()) {
                    if (child.getValue().nodes.isEmpty()) {
                        fieldSB.append("\"\"");
                        continue;
                    }

                    fieldSB.append("(");
                    boolean hasListData = false;
                    for (int i = 0; i < child.getValue().nodes.size(); ++i) {
                        if (hasListData) {
                            fieldSB.append(",");
                        }

                        if (isString) {
                            fieldSB.append("\"");
                        }
                        if (pickValueFieldCsvImpl(fieldSB, child.getValue().nodes.get(i))) {
                            hasListData = true;
                            if (isString) {
                                fieldSB.append("\"");
                            }
                        } else {
                            if (hasListData) {
                                fieldSB.deleteCharAt(fieldSB.length() - 1);
                            }
                            if (isString) {
                                // pop last quote
                                fieldSB.deleteCharAt(fieldSB.length() - 1);
                            }
                        }
                    }
                    fieldSB.append(")");
                } else if (!child.getValue().nodes.isEmpty()) {
                    if (isString) {
                        fieldSB.append("\"");
                    }
                    pickValueFieldCsvImpl(fieldSB, child.getValue().nodes.get(0));
                    if (isString) {
                        fieldSB.append("\"");
                    }
                }
            }

            // 需要补全空字段
            for (HashMap.Entry<String, DataDstFieldDescriptor> varPair : desc.getTypeDescriptor().fields.entrySet()) {
                String varName = getIdentName(varPair.getKey());
                if (dumpedFields.contains(varName)) {
                    continue;
                }

                fieldSB.append(",");
                fieldSB.append(getIdentName(varPair.getKey()));
                fieldSB.append("=");

                if (varPair.getValue().isList()) {
                    fieldSB.append("()");
                } else if (varPair.getValue().getType() == JAVA_TYPE.STRING
                        || varPair.getValue().getType() == JAVA_TYPE.BYTES) {
                    fieldSB.append("\"\"");
                } else {
                    pickValueFieldCsvDefaultImpl(fieldSB, varPair.getValue());
                }
            }

            fieldSB.append(")");
            return true;
        }

        Object val = pickValueFieldBaseImpl(desc);
        if (val == null) {
            if (desc.getFieldDescriptor() != null && desc.getFieldDescriptor().isList()) {
                return false;
            } else {
                return pickValueMessageCsvDefaultImpl(fieldSB, desc.getTypeDescriptor());
            }
        } else {
            fieldSB.append(val);
            return true;
        }
    }

    protected boolean pickValueMessageCsvDefaultImpl(StringBuffer sb, DataDstMessageDescriptor fd) {
        if (fd == null) {
            return false;
        }

        switch (fd.getType()) {
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE: {
            sb.append("0");
        }
        case BOOLEAN: {
            sb.append("False");
        }
        case STRING:
        case BYTES: {
            return false;
        }
        case MESSAGE: {
            sb.append("(");

            boolean isFirstField = true;
            for (HashMap.Entry<String, DataDstFieldDescriptor> varPair : fd.fields.entrySet()) {
                if (isFirstField) {
                    isFirstField = false;
                } else {
                    sb.append(",");
                }

                sb.append(getIdentName(varPair.getKey()));
                sb.append("=");
                if (varPair.getValue().isList()) {
                    sb.append("()");
                } else if (varPair.getValue().getType() == JAVA_TYPE.STRING
                        || varPair.getValue().getType() == JAVA_TYPE.BYTES) {
                    sb.append("\"\"");
                } else {
                    pickValueFieldCsvDefaultImpl(sb, varPair.getValue());
                }
            }

            sb.append(")");
            break;
        }
        default:
            return false;
        }

        return true;
    }

    protected boolean pickValueFieldCsvDefaultImpl(StringBuffer sb, DataDstFieldDescriptor fd) {
        if (fd.isList()) {
            return false;
        }

        return pickValueMessageCsvDefaultImpl(sb, fd.getTypeDescriptor());
    }
}
