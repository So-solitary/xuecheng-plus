package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@SpringBootTest
public class FreemarkerTest {

    @Autowired
    CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.getVersion());

        String classPath = this.getClass().getResource("/").getPath();
        System.out.println("classPath: " + classPath);

        configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));

        configuration.setDefaultEncoding("utf-8");

        Template template = configuration.getTemplate("course_template.ftl");

        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(2L);
        HashMap<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewDto);

        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        //System.out.println(content);

        InputStream inputStream = IOUtils.toInputStream(content);

        FileOutputStream outputStream = new FileOutputStream("C:\\temp\\test.html");

        IOUtils.copy(inputStream, outputStream);

    }
}
