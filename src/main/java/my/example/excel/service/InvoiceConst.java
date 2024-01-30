package my.example.excel.service;

import java.util.Arrays;
import java.util.List;

public class InvoiceConst {
    public final static String API_V1_INVOICE = "/api/v1/applications/invoice";
    public final static String INVOICE_ITEM_CM0 = "cm0";
    public final static String INVOICE_ITEM_ONE = "one";
    public final static String ACTIVE_Y = "Y";
    public final static String ACTIVE_N = "N";
    public final static double INVOICE_VAT = 1.1;

    public final static List<String> COMMON_TAGS = Arrays.asList(new String[] {INVOICE_ITEM_CM0,INVOICE_ITEM_ONE });
}
