package com.wtm.cashbon.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Created by @alimasudd on 9/12/2019.
 */
public class DecimalsFormat {

    static String[] angkaTerbilang = {"", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"};

    public static String priceWithDecimal(String price) {

        String result = "";

        try {

            Double ps = Double.parseDouble(price);
            DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

            formatRp.setCurrencySymbol("");
            formatRp.setDecimalSeparator(',');
            formatRp.setGroupingSeparator('.');
            DecimalFormat kursIndonesia = new DecimalFormat("###,###,##0.00", formatRp);
            kursIndonesia.setGroupingSize(3);
            kursIndonesia.setGroupingUsed(true);

            System.out.println("Harga Rupiah: " + kursIndonesia.format(ps) + " : " + ps);

            result = kursIndonesia.format(ps);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Harga Rupiah : " + price);

        }

        return result;
    }


    public static String priceWithDecimalOld(String price) {

        String result = price;

        try {

            Double doubleFromString = Double.parseDouble(price);
            DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
            result = formatter.format(doubleFromString);

        } catch (Exception e) {

        }

        return result;
    }


    public static String priceWithoutDecimal(String price) {

        String result = price;

        try {

            Double doubleFromString = Double.parseDouble(price);
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            result = formatter.format(doubleFromString);
        } catch (Exception e) {

        }

        return result;
    }


    public static String angkaToTerbilang(long angka) {

        String terbilang = angkaToTerbilangInteger(angka) + "";

        return terbilang.replace("sejuta","satu juta").replace("koma nol","");

    }

    public static String angkaToTerbilangInteger(Long angka) {
        if (angka < 12)
            return angkaTerbilang[angka.intValue()];
        if (angka >= 12 && angka <= 19)
            return angkaTerbilang[angka.intValue() % 10] + " Belas";
        if (angka >= 20 && angka <= 99)
            return angkaToTerbilangInteger(angka / 10) + " Puluh " + angkaTerbilang[angka.intValue() % 10];
        if (angka >= 100 && angka <= 199)
            return "Seratus " + angkaToTerbilangInteger(angka % 100);
        if (angka >= 200 && angka <= 999)
            return angkaToTerbilangInteger(angka / 100) + " Ratus " + angkaToTerbilangInteger(angka % 100);
        if (angka >= 1000 && angka <= 1999)
            return "Seribu " + angkaToTerbilangInteger(angka % 1000);
        if (angka >= 2000 && angka <= 999999)
            return angkaToTerbilangInteger(angka / 1000) + " Ribu " + angkaToTerbilangInteger(angka % 1000);
        if (angka >= 1000000 && angka <= 999999999)
            return angkaToTerbilangInteger(angka / 1000000) + " Juta " + angkaToTerbilangInteger(angka % 1000000);
        if (angka >= 1000000000 && angka <= 999999999999L)
            return angkaToTerbilangInteger(angka / 1000000000) + " Milyar " + angkaToTerbilangInteger(angka % 1000000000);
        if (angka >= 1000000000000L && angka <= 999999999999999L)
            return angkaToTerbilangInteger(angka / 1000000000000L) + " Triliun " + angkaToTerbilangInteger(angka % 1000000000000L);
        if (angka >= 1000000000000000L && angka <= 999999999999999999L)
            return angkaToTerbilangInteger(angka / 1000000000000000L) + " Quadrilyun " + angkaToTerbilangInteger(angka % 1000000000000000L);
        return "";
    }

    /*public static String priceToString(Double price) {
        String toShow = priceWithoutDecimal(price);
        if (toShow.indexOf(".") > 0) {
            return priceWithDecimal(price);
        } else {
            return priceWithoutDecimal(price);
        }
    }*/
}
