# TestYouTube
集成YouTube播放器测试

通过YouTube提供的搜索API搜索视频资源，用YouTubePlayer和EXOPlayer播放。

搜索结果SearchResult中videoId是这样的{"kind":"youtube#video","videoId":"EU5JlkRYPh8"}，
可以直接搜索setQ("EU5JlkRYPh8")，这样搜索出来的就是这个ID的资源；
使用YouTubePlayer.cueVideo("EU5JlkRYPh8");就可以直接播放了。

最蛋疼的是使用第三方的播放器，如EXOPlayer：
其他播放器是不能直接播放搜索API返回的结果，就算是用"https://www.youtube.com/watch?v=" + videoid这样拼接也是不能播放的，所以需要再次解析视频资源。
不建议个人写解析过程，太复杂了，其实就用使用http工具去请求上边拼接的地址，然后解析返回数据，从中拿到可以播放的视频。
项目中用'com.github.HaarigerHarald:android-youtubeExtractor:master-SNAPSHOT'这个解析工具，使用还算方便：
```
        new YouTubeExtractor(holder.iv_photo.getContext()) {

            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                if (ytFiles != null) {
                    String url = null;
                    //返回的结果中会有不同尺寸的视频，可以根据需要去选择
                    for (int i = 0, size = ytFiles.size(); i < size; i++) {
                        if (ytFiles.get(i) != null) {
                            url = ytFiles.get(i).getUrl();
                            // 这是其中一段log：Format = Format{itag=18, ext='mp4', height=360, fps=30, vCodec=null, aCodec=null, audioBitrate=96, isDashContainer=false, isHlsContent=false}, url = https://r1---sn-ipoxu-un5s.googlevideo.com/videoplayback?expire=1566921200&ei=kP1kXZXgMIfgqQGPnpFo&ip=61.222.32.25&id=o-AAUvz8eS3dUA_gxOjfWOjUSFTwnGnmD72ivVDa3La_1K&itag=18&source=youtube&requiressl=yes&mm=31%2C29&mn=sn-ipoxu-un5s%2Csn-un57sn7s&ms=au%2Crdu&mv=m&mvi=0&pl=24&initcwndbps=1163750&mime=video%2Fmp4&gir=yes&clen=21312193&ratebypass=yes&dur=359.862&lmt=1557569508280291&mt=1566899509&fvip=5&c=WEB&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cmime%2Cgir%2Cclen%2Cratebypass%2Cdur%2Clmt&sig=ALgxI2wwRgIhAK1NJR5va9ENHMXRwxd6ZBTj_DeFCWfTiDmIPeOqkEk2AiEAmiP32W6w7jPRMnpmstel-Tsez-GfH7D3DSTPG4ddj20%3D&lsparams=mm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AHylml4wRQIhAInqtw-eNm9EI2nyArD-kUgmooqQxPF-rQlQhNZu0VumAiA5kw2w8hGLwf_CcZM5jOpvLlxAj-lu6UNL8KXwccg2LQ%3D%3D
                            Log.i("result", "onExtractionComplete: Format = " + ytFiles.get(i).getFormat() + ", url = " + ytFiles.get(i).getUrl());
                        }
                    }

                    //根据需求选择不同尺寸的视频地址去播放，我这里随便选了一个
                    if (!TextUtils.isEmpty(url)) {
                        Uri uri = Uri.parse(url);
                        MediaSource source = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("exoplayer-codelab")).createMediaSource(uri);
                        player.prepare(source);
                    }
                }
            }
        }.extract("https://www.youtube.com/watch?v=" + data.get(i).getVideoID(), true, true);

```
