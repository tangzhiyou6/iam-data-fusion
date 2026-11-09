package com.hd;

import com.hd.scheduler.CXTZScheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IamDataApps {
    public static void main(String[] args) {
        /*String fileId = "f177905122bc47b8bd9a7199556abbbc";
        if (null != args && args.length > 0) {
            fileId = args[0];
        }*/

       ConfigurableApplicationContext run = SpringApplication.run(IamDataApps.class, args);
    //    FileDownloadResult result = null;

        CXTZScheduler bean = run.getBean(CXTZScheduler.class);
        bean.extractStructuredData();
        System.exit(0);


        /*FileDownloadResult result = null;

        EaucClient eaucClient = run.getBean(EaucClient.class);
        SourceFileDownloadParamsQo qo = SourceFileDownloadParamsQo.builder().fileId("c18cd0ddf32b400c943843b4652226b7")
                .fileType("auctions_matter_base").build();
        result = eaucClient.downloadAttaFile(qo);
        System.out.println(JsonUtils.toJSON(result));
        FileDownloadUtils.saveToDisk(result, Paths.get("/opt/p-eauc"));
        System.exit(0);*/

        //司库下载测试
        /*SkScheduler skClient = run.getBean(SkScheduler.class);
        String[] fileArrays = fileDatas.split("\n");;
        for (String remotePath : fileArrays) {
            System.out.println("下载文件：" + remotePath);
            if (StringUtils.isEmpty(remotePath)) {
                continue;
            }
            SkAttachmentVo skAttachmentVo = new SkAttachmentVo();
            skAttachmentVo.setFilePath(remotePath.trim());
            skClient.uploadOss(skAttachmentVo);
        }

        System.exit(0);*/

        /*DnygFusionService dnygFusionService = run.getBean(DnygFusionServiceImpl.class);
        DnygClient dnygClient = run.getBean(DnygClient.class);

        List<DnygAttachmentVo> attachments = dnygFusionService.findAttachments(null);
        for (DnygAttachmentVo vo : attachments) {
            if (StringUtils.isNotEmpty(vo.getInvUrl())) {
                FileDownloadResult download = dnygClient.download(vo.getInvUrl());
                if (null != download) {
                    FileDownloadUtils.saveToDisk(download, "E:\\windows\\Users\\sulin\\Desktop\\11111");
                }
            }

        }*/


        // BaiRuiXTServiceImpl brxtService = run.getBean(BaiRuiXTServiceImpl.class);
        // List<BaiRuiXtAttachmentVo> lists = brxtService.findAttachments("财务尽调", 0L);
        // System.out.println(lists);

        // BaiRuiXTClient brxtClient = run.getBean(BaiRuiXTClient.class);
        // for (BaiRuiXtAttachmentVo  vo : lists) {
        //     FileDownloadResult fileDownloadResult = brxtClient.freeDownload(vo.getId());
        //     if (null != fileDownloadResult) {
        //         FileDownloadUtils.saveToDisk(fileDownloadResult, "/opt/brxt");
        //     }
        // }

    //    File file = new File("E:\\windows\\Users\\sulin\\Desktop\\brx111t.zip");
        // List<ArchiveExtractorVo> extract = ArchiveExtractor.extract(file);
        // System.out.println(extract);
       // System.exit(0);

        // Set<String> fileIds = Sets.newHashSet("1001", "1002","1003","1001","1001","1006","1007");
/*        Set<String> fileIds = Sets.newHashSet("sbbceccf1c3011e7816d8fafc29309c7");
        for (String fileId : fileIds) {
            GdtbackClient bean = run.getBean(GdtbackClient.class);
            result = bean.freeDownload(fileId);
            if (result != null) {
                Path savedPath = FileDownloadUtils.saveToDisk(result, "/data/brxt");
                System.out.println("文件已保存: " + savedPath.toAbsolutePath());
            }
        }*/

/*        try {
            org.springframework.core.env.Environment env = run.getEnvironment();
            BigFilePostDownloadTestUtil.DownloadConfig config =
                    BigFilePostDownloadTestUtil.DownloadConfig.builder()
                            .downloadUrl(env.getProperty("file-sync.gdtback.free-download"))
                            .username(env.getProperty("file-sync.gdtback.username"))
                            .password(env.getProperty("file-sync.gdtback.password"))
                            .secret(env.getProperty("file-sync.gdtback.secret"))
                            .threadCount(20)
                            .chunkSize(64L * 1024 * 1024)
                            .maxRetries(10)
                            .build();
            Set<String> fileIds = Sets.newHashSet("v76ce68f9ad411e883d900505688bf34", "1004");
            for (String fileId : fileIds) {
                //下载：
                String fileName = "/data/20260728001/" + fileId +"_" + DateUtils.getFormatDateTime(new Date(), YYYYMMDDHHMMSS_FORMAT_PATTERN) + ".7z";
                BigFilePostDownloadTestUtil.download(fileId, fileName, config);
                System.out.println("下载完成！\n\n\n\n\n");

                //解压
                Set<String> heatInfos = ConcurrentHashMap.newKeySet();
                BigSevenZExtractorUtils sevenZExtractorUtils = new BigSevenZExtractorUtils();
                sevenZExtractorUtils.extract(new File(fileName), new File(fileName.substring(0, fileName.lastIndexOf("."))),
                        progress -> {
                            String progressInfo = String.format("[%d%%] %s%n", progress.getPercent(), progress.getCurrentFile());
                            if (!heatInfos.contains(progressInfo)) {
                                System.out.print(progressInfo);
                                heatInfos.add(progressInfo);
                            }
                        });
                System.out.println("解压完成！");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/


        /*BrxtScheduler bean = run.getBean(BrxtScheduler.class);
        bean.tempDeleteAll();
        System.exit(0);*/
    }


    private static String fileDatas = "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200727183508866 合同 工行斗古瓦厂.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200727184119355 松木坎项目贷款合同.doc\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200727180656325 招商局直租1.011亿CMCL-ZL-2019-096-RZ-01.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/2020072718473590 仙水窝项目贷款合同.doc\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/2020072114504019 建行20000万元202003.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200727190204445 金元1.5亿合同.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200720154039717 116025CW0320200024-光大银行流动资金贷款合同.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200727185429196 金元委贷2.5亿2020-4-24.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200728142110185 9.9亿国开行（202006030.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/2020072114504019 建行20000万元202004.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200727173348539 招商局天津2.9亿CMCL-TJ-ZL-2019-037-RZ-02.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200728154452267 10亿邮储银行.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/2020072816271215 固定资产贷款合同（半坡）.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200728193336666 15亿工商银行.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/2020072909073549 固定资产贷款合同（大坪子）.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200729101246224 固定资产贷款合同（五嘎）.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/2020072114504019 建行20000万元202005.pdf\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200729103047810 麻乍乐利借款合同.docx\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200729103047812 收费权益质押合同.docx\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200729103047814 应收帐款质押登记协议.docx\n" +
            "/data/domains/attach/g6/file/GDEBIT@gdt_conLiquidity_btn.submit/WNNYRZTZDJG/20200729103047816 账户监管协议.docx";
}
