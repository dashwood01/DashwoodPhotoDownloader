# DashwoodPhotoDownloader

[![](https://jitpack.io/v/dashwood01/DashwoodPhotoDownloader.svg)](https://jitpack.io/#dashwood01/DashwoodPhotoDownloader)

#### You can use this library download and show image file also this library support ssl url

```gradle
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    
```

```gradle
    	        implementation 'com.github.dashwood01:DashwoodPhotoDownloader:0.4'
```

### Use it like this

```java
    PhotoDownloader photoDownloader=new PhotoDownloader(context,url
        ,imageView);
        //Be careful you must execute this function at the end
        photoDownloader.init();
```

## Options

Options | What work
------------ | -------------
setImageLoading | Loading image show when image in downloading
setUnloadedImage | If image not download show this
clearCache | If you want every time delete image call this function
setQuality | Quality of photo
setWidthAndHeight | Width and Height of image
setFilePath | You can save custom path you want
setCallBackDownloader | CallBackDownloader listener
setTimeForDeleteImage | You can use this function for delete files from now after minutes you want, If you don't set this item as default delete every hour
setHeader | You can now add header for security download
