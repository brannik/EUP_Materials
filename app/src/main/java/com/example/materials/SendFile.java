package com.example.materials;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class SendFile {
    SharedPreffManager manager;
    File externalFilesDirectory;
    File textFile;
    FileWriter writer;
    CharSequence s;
    private Context context;
    private float WarningLevel;
    // templates
    String HTML_HEADER = "Today is {0}";
    String HTML_BODY = "";
    String HTML_END = "";

    ArrayList<File> files = new ArrayList<>();
    public SendFile(Context _context){
        context = _context;
        manager = new SharedPreffManager(context);
        WarningLevel = manager.GetWarningValue();

        Date d = new Date();
        s = DateFormat.format("MMMM d, yyyy ", d.getTime());
    }
    public void Send(){
        boolean isPDF = manager.getCheckButtonState("PDF");
        boolean isHTML = manager.getCheckButtonState("HTML");
        boolean isDOCX = manager.getCheckButtonState("DOCX");
        boolean isEXEL = manager.getCheckButtonState("EXEL");
        if(isPDF)
            GeneratePdf();
        if(isHTML)
            GenerateHtml();
        if(isDOCX)
            GenerateDocx();
        if(isEXEL)
            GenerateExel();
        SendToEmail(files,String.format("MB-%s",s));
    }
    private void GenerateExel(){

    }
    private void GenerateHtml(){
        String filename = String.format("Materials-%s.html",s);

        externalFilesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        //Log.w("WARNING", externalFilesDirectory.getAbsolutePath());
        textFile = new File(externalFilesDirectory,filename);


        StringBuilder sb = new StringBuilder("<!DOCTYPE html><html><head>" +
                "<meta content='text/html; charset=UTF-8' http-equiv='Content-Type'>"+
                "<style>" +
                "body{background-color: black;}" +
                "table,th,td{background-color: white; border: 1px solid #000000;}" +
                "td{color: black; font-size: 32px;}" +
                ".amount{text-align: center;}" +
                "#low{color: white; background-color: red;}" +
                "</style>"+
                "</head><body><table><tr><th colspan='2'>" + filename + "</th></tr>");

        sb.append("<tr><td>Материал</td><td>Количество</td></tr>");
        for(String key : manager.GetPrefs().getAll().keySet()){
            float value = manager.GetPrefs().getFloat(key,0.0f);
            if(value <= WarningLevel){
                // if amount is less than 50kg mark with red color
                sb.append(String.format("<tr><td id='low'>%s</td><td class='amount' id='low'>%s КГ</td></tr>",key,manager.GetPrefs().getFloat(key,0.0f)));
            }else{
                sb.append(String.format("<tr><td>%s</td><td class='amount'>%s КГ</td></tr>",key,manager.GetPrefs().getFloat(key,0.0f)));
            }
        }
        sb.append("</table><button onclick=\"window.print()\">Принтирай страницата</button></body></html>");
        String message = sb.toString();

        try {
            writer = new FileWriter(textFile);
            writer.append(message);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.getLocalizedMessage();
        }
        //###############################################
        files.add(textFile);

    }
    private void GeneratePdf(){
        String filename = String.format("Materials-%s.pdf",s);
        String extstoragedir = Environment.getExternalStorageDirectory().toString();
        File fol = new File(extstoragedir, "pdf");
        File folder=new File(fol,"pdf");
        if(!folder.exists()) {
            boolean bool = folder.mkdir();
        }
        try {
            final File file = new File(folder, filename);
            files.add(file);
            file.createNewFile();

            FileOutputStream fOut = new FileOutputStream(file);


            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new
                    PdfDocument.PageInfo.Builder(100, 100, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            canvas.drawText("Test pdf data here.", 10, 10, paint);

            document.finishPage(page);
            document.writeTo(fOut);
            document.close();

        }catch (IOException e){
            Log.i("error",e.getLocalizedMessage());
        }
    }
    private void GenerateDocx(){

    }

    public void SendToEmail(ArrayList<File> _files,String _subject){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent .setType("vnd.android.cursor.dir/email");
        for(File _file : _files){
            Uri fileUri = FileProvider.getUriForFile(context,context.getPackageName()+".provider",_file);
            emailIntent .putExtra(Intent.EXTRA_STREAM, fileUri);
        }

        Map<String,?> keys = manager.GetEmailPrefs().getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()) {
            emailIntent .putExtra(Intent.EXTRA_EMAIL, new String[]{entry.getValue().toString()});
        }

        emailIntent .putExtra(Intent.EXTRA_SUBJECT, _subject);
        //emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

    private void SaveToHistory(ArrayList<File> _files, CharSequence _date){
        // save files localy
        // save data to the sharedpreffs - new part with history only
    }
}