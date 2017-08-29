/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hugosama.controllers;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentController {
    
    @RequestMapping(path="/generate/doc",method = RequestMethod.GET)
    public String generateDocument() throws IOException {
            ReportBuilder reportBuilder = new ReportBuilder();
        ReportTemplateBuilder reportTemplateBuilder = new ReportTemplateBuilder()
                .documentPath("./src/main/resources/invoice.docx")
                .documentName("invoice.docx")
                .outputType(ReportOutputType.docx)
                .readFileFromPath();
        reportBuilder.template(reportTemplateBuilder.build());
        BandBuilder bandBuilder = new BandBuilder();
        String json = "{ \"main\" : " +
                "                              { " +
                "                               \"invoiceNumber\":99987" +
                "                            }}";
        System.out.println(json);
        ReportBand main = bandBuilder.name("Main").query("Main", json, "json", "parameter=param1 &.main").build();


        bandBuilder = new BandBuilder();
        ReportBand items = bandBuilder.name("Items").query("Items", "return [\n" +
                "                                ['name':'Java Concurrency in practice', 'price' : 15000],\n" +
                "                                ['name':'Clear code', 'price' : 13000],\n" +
                "                                ['name':'Scala in action', 'price' : 12000]\n" +
                "                            ]", "groovy").build();

        reportBuilder.band(main);
        reportBuilder.band(items);
        reportBuilder.format(new ReportFieldFormatImpl("Main.signature", "${html}"));
        reportBuilder.format(new ReportFieldFormatImpl("Main.footer", "${html}"));

        Report report = reportBuilder.build();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(
                new DefaultLoaderFactory().setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
        .setJsonDataLoader(new JsonDataLoader()));

        ReportOutputDocument reportOutputDocument = reporting.runReport(
                new RunParams(report).param("param1", json), new FileOutputStream("./src/main/resources/output.docx"));
        
        return "{}";
    }
    
}
