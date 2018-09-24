# SuRichText
## 1.效果图
![](https://github.com/1249848166/SuRichText/blob/master/QQ%E8%A7%86%E9%A2%9120180922140116.gif)
![](https://github.com/1249848166/SuRichText/blob/master/QQ%E8%A7%86%E9%A2%9120180922140116%20(1).gif)
## 2.使用
1.添加依赖
```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
```java
dependencies {
	        implementation 'com.github.1249848166:SuRichText:1.2'
	}
```
2.使用
```java
richTextEditor=new SuRichTextEditor(this, (LinearLayout) findViewById(R.id.container));//初始化编辑器（第一个参数是context，第二个参数是容器，也就是容纳视图的根布局，必须是一个scrollview里面的linearlayout，具体可以看我代码中的布局）

richTextEditor.SearchFileDatas(SuRichTextEditor.TYPE_PICTURE, null);//调用这个方法前往图片选择器
richTextEditor.SearchFileDatas(SuRichTextEditor.TYPE_AUDIO, null);//调用这个方法前往音频选择器
richTextEditor.SearchFileDatas(SuRichTextEditor.TYPE_VIDEO, null);//调用这个方法前往视频选择器

//然后从选择器的回调中可以获得选择图片，音频，视频的路径
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==SuRichTextEditor.CODE_REQUEST_VIDEO){
                List<String> paths=data.getStringArrayListExtra("paths");
                for(String path:paths){
                    System.out.println("得到视频路径："+path);
                    richTextEditor.insertVideo(path);//将视频插入编辑器中
                }
            }else if(requestCode== CODE_REQUEST_PICTURE){
                List<String> paths=data.getStringArrayListExtra("paths");
                for(String path:paths){
                    System.out.println("得到图片路径："+path);
                    richTextEditor.insertImage(path);//将图片插入编辑器中
                }
            }else if(requestCode==CODE_REQUEST_AUDIO){
                List<String> paths=data.getStringArrayListExtra("paths");
                for(String path:paths){
                    System.out.println("得到音频路径："+path);
                    richTextEditor.insertAudio(path);//将音频插入编辑器中
                }
            }
        }
    }
    
    //提交所有内容到服务器（由于上传到服务器受到方式的限制，因此将这一部分开放给使用者写具体上传的代码，也很简单）
    richTextEditor.submit(title,author,others,//这三个参数，分别是标题，作者，其它信息（毕竟是文章，一些基本信息还是需要的）
                new SuUploadImage() {//提交图片获得url（这个回调让你写上传图片代码，获得url然后设置到callback里面）
            @Override
            public void upload(String path, final int index, final SuUploadCallback suUploadCallback) {
                //这里填写将图片上传到服务器的代码，会得到一个url
                BmobUtil.getInstance().uploadFile(new String[]{path}, "", new BmobUtil.UploadFileCallback() {
                    @Override
                    public void onUploadFile(String url) {
                        suUploadCallback.onReturn(index,url);//将得到的url返回
                        System.out.println(url);
                    }
                });
            }
        }, new SuUploadAudio() {//提交音频获得url（这个回调让你写上传音频代码，获得url然后设置到callback里面）
            @Override
            public void upload(String path, final int index, final SuUploadCallback suUploadCallback) {
                //这里填写将音频上传到服务器的代码，会得到一个url
                BmobUtil.getInstance().uploadFile(new String[]{path}, "", new BmobUtil.UploadFileCallback() {
                    @Override
                    public void onUploadFile(String url) {
                        suUploadCallback.onReturn(index,url);//将得到的url返回
                        System.out.println(url);
                    }
                });
            }
        }, new SuUploadVideo() {//提交视频获得url（这个回调让你写上传视频代码，获得url然后设置到callback里面）
            @Override
            public void upload(String path, final int index, final SuUploadCallback suUploadCallback) {
                //这里填写将视频上传到服务器的代码，会得到一个url
                BmobUtil.getInstance().uploadFile(new String[]{path}, "", new BmobUtil.UploadFileCallback() {
                    @Override
                    public void onUploadFile(String url) {
                        suUploadCallback.onReturn(index,url);//将得到的url返回
                        System.out.println(url);
                    }
                });
            }
        },new SuSubmit() {//最后一步提交（这个回调是最终把所有内容整合成的字符窜上传到最后服务器）
            @Override
            public void submit(String title,String author,String content,String others, final SuResultCallback suResultCallback) {
                //这里填写最后将文本，图片url，音频url，视频url提交到服务器的代码
                BmobUtil.getInstance().uploadContent(title, content, new BmobUtil.UploadContentCallback() {
                    @Override
                    public void onUploadContent(boolean success) {
                        suResultCallback.onResult(success);//通知结果
                    }
                });
                System.out.println("最后整合成的文本:"+content);
            }
        }, new SuResultCallback() {//最后提交的返回结果，成功或失败（在这里可以提醒上传结果）
            @Override
            public void onResult(boolean success) {
                updating_progress.setVisibility(View.INVISIBLE);
                updating_text.setVisibility(View.INVISIBLE);
                finish.setVisibility(View.VISIBLE);
                //返回结果
                if(success) {
                    Toast.makeText(WriteActivity.this, "提交成功" , Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(WriteActivity.this, "提交失败" , Toast.LENGTH_SHORT).show();
                }
            }
        });
        //之所以上传部分按照类型分开，考虑到不同内容性能，比如专门的视频服务器肯定更好，所以分开，可以按照自己的意愿选择多种服务器或者只用一个。
        
        //上面是编辑内容并上传的部分，下面是显示已经上传内容
        //很简单
        richText.setContent(getIntent().getStringExtra("html"));//setcontent传入的参数是所有内容组成的字符串，内部会自动分割出每一个部分，是不是很简单。。。只需一步。。。但是必须注意这个的布局也要求scrollview里面放linearlayout，具体见示例代码
```
3.注意事项
记得添加权限（比如文件读写，网络等）
## 3.小缺陷
1.选择器界面写死，后续应当开放出界面代码，让用户自定义风格
2.音频，视频样式写死，后续也应当让用户可以自定义界面
3.在上传的过程中，如果删除或添加内容，会出现排版错乱问题，因为数据是上传结束后才清空。应该保证上传过程中不可编辑（这是小问题），其实更应该是，上传时退出界面，转入后台上传，这个有待实现。
