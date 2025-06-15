package com.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.fileupload2.jakarta.JakartaServletRequestContext;

import com.bean.Category;
import com.bean.Goods;
import com.daofactory.DaoFactory;

@WebServlet("/GoodsServlet")
public class GoodsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 上传文件配置
    private static final String UPLOAD_DIRECTORY = "upload";
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 10;  // 10MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 100;     // 100MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 120;  // 120MB

    public GoodsServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        Goods good = new Goods();
        String msg = "";
        boolean result = false;
        int gid = 0;
        int cid = 1;
        String path = "error.jsp";
        ArrayList<Goods> tuijianGoods = new ArrayList<Goods>();
        ArrayList<Goods> cainixihGoods = new ArrayList<Goods>();
        ArrayList<Category> categories = new ArrayList<Category>();
        String type = request.getParameter("type");

        String gname = "";
        double price = 0.0;
        double inPrice = 0.0;
        String introduction = "";
        int stock = 0;
        String fileName = "";
        String picture = "";
        double highPraise = 0.0;
        int sales = 0;

        if (request.getParameter("gid") != null)
            gid = Integer.parseInt(request.getParameter("gid"));
        if (request.getParameter("cid") != null)
            cid = Integer.parseInt(request.getParameter("cid"));

        if (type.equals("viewbygid")) {
            try {
                good = DaoFactory.getGoodsDaoInstance().getById(gid);
                tuijianGoods = DaoFactory.getGoodsDaoInstance().pxgetbyCidQsan(cid);
                cainixihGoods = DaoFactory.getGoodsDaoInstance().pxgetbyCidSalesidianwu(cid);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (good.getXqPicture() == null) {
                good.setXqPicture("");
            } else {
                String img = good.getXqPicture();
                String[] pic = img.split(";");
                request.setAttribute("img", pic);
            }

            if (good.getZsPicture() == null) {
                good.setZsPicture("");
            } else {
                String img = good.getZsPicture();
                String[] pic = img.split(";");
                request.setAttribute("zsimg", pic);
            }

            request.setAttribute("good", good);
            request.setAttribute("tuijianGoods", tuijianGoods);
            request.setAttribute("cainixihGoods", cainixihGoods);
            path = "proDetail.jsp";
        } else if (type.equals("viewbygidadmin")) {
            String quanxian = request.getParameter("quanxian");
            if (quanxian.equals("admin")) {
                try {
                    good = DaoFactory.getGoodsDaoInstance().getById(gid);
                    categories = DaoFactory.getCategoryDaoInstance().getAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < categories.size(); i++) {
                    if (good.getCid() == categories.get(i).getCid()) {
                        good.setCidname(categories.get(i).getCname());
                        categories.remove(i);
                    }
                }

                if (good.getXqPicture() == null) {
                    good.setXqPicture("");
                } else {
                    String img = good.getXqPicture();
                    String[] pic = img.split(";");
                    request.setAttribute("img", pic);
                }

                if (good.getZsPicture() == null) {
                    good.setZsPicture("");
                } else {
                    String img = good.getZsPicture();
                    String[] pic = img.split(";");
                    request.setAttribute("zsimg", pic);
                }

                request.setAttribute("good", good);
                request.setAttribute("categories", categories);
                path = "goodupdate.jsp";
            }
        } else if (type.equals("insertGood")) {
            try {
                categories = DaoFactory.getCategoryDaoInstance().getAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
            request.setAttribute("categories", categories);
            path = "goodInsert.jsp";
        } else if (type.equals("insertSJGood")) {
            // 使用Jakarta文件上传API
            if (!JakartaServletFileUpload.isMultipartContent(request)) {
                PrintWriter writer = response.getWriter();
                writer.println("Error: 表单必须包含 enctype=multipart/form-data");
                writer.flush();
                return;
            }

            DiskFileItemFactory factory = DiskFileItemFactory.builder()
                    .setBufferSize(MEMORY_THRESHOLD)
                    .setPath(Paths.get(System.getProperty("java.io.tmpdir")))
                    .get();

            JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_REQUEST_SIZE);
            upload.setHeaderCharset(Charset.defaultCharset());

            String uploadPath = request.getServletContext().getRealPath("/") + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            try {
                List<DiskFileItem> items = upload.parseRequest(new JakartaServletRequestContext(request));

                for (DiskFileItem item : items) {
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getString(Charset.defaultCharset());

                        switch (fieldName) {
                            case "gname":
                                gname = fieldValue;
                                break;
                            case "price":
                                price = Double.parseDouble(fieldValue);
                                break;
                            case "inPrice":
                                inPrice = Double.parseDouble(fieldValue);
                                break;
                            case "stock":
                                stock = Integer.parseInt(fieldValue);
                                break;
                            case "cid":
                                cid = Integer.parseInt(fieldValue);
                                break;
                            case "introduction":
                                introduction = fieldValue;
                                break;
                        }
                    } else {
                        fileName = Paths.get(item.getName()).getFileName().toString();
                        String filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);
                        item.write(storeFile.toPath());
                        picture = UPLOAD_DIRECTORY + "/" + fileName;
                        request.setAttribute("picture", picture);
                    }
                }
            } catch (Exception ex) {
                request.setAttribute("message", "错误信息: " + ex.getMessage());
            }

            good.setGname(gname);
            good.setInPrice(inPrice);
            good.setPrice(price);
            good.setIntroduction(introduction);
            good.setCid(cid);
            good.setStock(stock);
            good.setPicture(picture);

            try {
                result = DaoFactory.getGoodsDaoInstance().add(good);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) path = "goodsManagement.jsp";
        } else if (type.equals("delete")) {
            try {
                result = DaoFactory.getGoodsDaoInstance().delete(gid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) path = "goodsManagement.jsp";
        } else if (type.equals("deleteAll")) {
            String[] ids = request.getParameterValues("all");
            StringBuilder condition = new StringBuilder();
            for (int i = 0; i < ids.length; i++) {
                condition.append(ids[i]);
                if (i != ids.length - 1) condition.append(",");
            }
            try {
                result = DaoFactory.getGoodsDaoInstance().deleteAll(condition.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) path = "goodsManagement.jsp";
        } else if (type.equals("uptade")) {
            if (!JakartaServletFileUpload.isMultipartContent(request)) {
                PrintWriter writer = response.getWriter();
                writer.println("Error: 表单必须包含 enctype=multipart/form-data");
                writer.flush();
                return;
            }

            DiskFileItemFactory factory = DiskFileItemFactory.builder()
                    .setBufferSize(MEMORY_THRESHOLD)
                    .setPath(Paths.get(System.getProperty("java.io.tmpdir")))
                    .get();

            JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_REQUEST_SIZE);
            upload.setHeaderCharset(Charset.defaultCharset());

            String uploadPath = request.getServletContext().getRealPath("/") + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            try {
                List<DiskFileItem> items = upload.parseRequest(new JakartaServletRequestContext(request));

                for (DiskFileItem item : items) {
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getString(Charset.defaultCharset());

                        switch (fieldName) {
                            case "gid":
                                gid = Integer.parseInt(fieldValue);
                                break;
                            case "gname":
                                gname = fieldValue;
                                break;
                            case "price":
                                price = Double.parseDouble(fieldValue);
                                break;
                            case "inPrice":
                                inPrice = Double.parseDouble(fieldValue);
                                break;
                            case "stock":
                                stock = Integer.parseInt(fieldValue);
                                break;
                            case "cid":
                                cid = Integer.parseInt(fieldValue);
                                break;
                            case "introduction":
                                introduction = fieldValue;
                                break;
                            case "highPraise":
                                highPraise = Double.parseDouble(fieldValue);
                                break;
                            case "sales":
                                sales = Integer.parseInt(fieldValue);
                                break;
                        }
                    } else {
                        fileName = Paths.get(item.getName()).getFileName().toString();
                        String filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);
                        item.write(storeFile.toPath());
                        picture = UPLOAD_DIRECTORY + "/" + fileName;
                        request.setAttribute("picture", picture);
                    }
                }
            } catch (Exception ex) {
                request.setAttribute("message", "错误信息: " + ex.getMessage());
            }

            good.setGid(gid);
            good.setGname(gname);
            good.setPrice(price);
            good.setInPrice(inPrice);
            good.setIntroduction(introduction);
            good.setStock(stock);
            good.setCid(cid);
            good.setPicture(picture);
            good.setHighPraise(highPraise);
            good.setSales(sales);

            try {
                result = DaoFactory.getGoodsDaoInstance().update(good);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) path = "goodsManagement.jsp";
        }

        request.setAttribute("msg", msg);
        request.getRequestDispatcher(path).forward(request, response);
    }
}