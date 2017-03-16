package com.lg.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ContractController {

    @Value("${file.attachmentPath}")
    String attachmentPath;

    static Map<Integer, String> fileMaps = new HashMap<>();
    static {
        fileMaps.put(1,"买卖合同");
        fileMaps.put(2,"供给能源合同");
        fileMaps.put(3,"借款合同");
        fileMaps.put(4,"担保合同");
        fileMaps.put(5,"租赁与融资租赁");
        fileMaps.put(6,"加工承揽合同");
        fileMaps.put(7,"建设工程合同");
        fileMaps.put(8,"运输合同");
        fileMaps.put(9,"技术合同");
        fileMaps.put(10,"委托行纪居间合同");
        fileMaps.put(11,"涉外合同");
        fileMaps.put(12,"保险合同");
        fileMaps.put(13,"银行业务合同");
        fileMaps.put(14,"信托合同");
        fileMaps.put(15,"证券期货合同");
        fileMaps.put(16,"网络信息技术合同");
        fileMaps.put(17,"知识产权合同");
        fileMaps.put(18,"教育文化合同");
        fileMaps.put(19,"娱乐体育合同");
        fileMaps.put(20,"海事海商合同");
        fileMaps.put(21,"服务合同");
        fileMaps.put(22,"企业组织经营合同");
        fileMaps.put(23,"劳动关系合同");
        fileMaps.put(24,"身份关系协议");
        fileMaps.put(25,"其它协议");
    }

    @RequestMapping(value = "category",method = RequestMethod.GET)
    public String category(Model model){
        model.addAttribute("categories", fileMaps);
        return "category";
    }

    @RequestMapping(value = "/contract/{category}",method = RequestMethod.GET)
    public String showProduct(Model model,@PathVariable Integer category){
        String dirName = fileMaps.get(category);
        File dir  = new File(attachmentPath+File.separator+"sdl_docs"+File.separator+dirName);
        File[]   files = dir.listFiles();
        List<String> fileNames = Arrays.stream(files).map(file -> file.getName()).collect(Collectors.toList());
        model.addAttribute("contracts", fileNames);
        model.addAttribute("category", category);
        return "contracts";
    }
    /**
     * 附件下载
     * @param contractId
     * @param fileName
     * @throws Exception
     */
    @RequestMapping(value = "/contract/download/{contractId}/{fileName}",method = RequestMethod.GET)
    public void attachmentDownload(HttpServletResponse res,
                                              @PathVariable int contractId,
                                              @PathVariable String fileName) {
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        try {
            res.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8")+".doc" );

            File file = new File(attachmentPath+ File.separator +"sdl_docs"+ File.separator
                    +fileMaps.get(contractId)
                    +File.separator+ fileName+".doc" );
            OutputStream os = null;

                os = res.getOutputStream();

                BufferedOutputStream bos = new BufferedOutputStream(os);

                InputStream is = null;

                is = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);

                int length = 0;
                byte[] temp = new byte[1 * 1024 * 10];

                while ((length = bis.read(temp)) != -1) {
                    bos.write(temp, 0, length);
                }
                bos.flush();
                bis.close();
                bos.close();
                is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
