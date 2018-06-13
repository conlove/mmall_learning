<html>
<body>
<h2>springmvc upload file</h2>
<form name="form1" action="/manage/product/upload.do" enctype="multipart/form-data" method="post">
    <input name="upload_file" type="file">
    <input value="springmvc图片上传" type="submit">
</form>
<h2>richtext upload file</h2>
<form name="form2" action="/manage/product/richtext_img_upload.do" enctype="multipart/form-data" method="post">
    <input name="upload_file" type="file">
    <input value="richtext图片上传" type="submit">
</form>
</body>
</html>