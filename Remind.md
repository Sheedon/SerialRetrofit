# MqttDispatcher

### Gradle

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```
	dependencies {
	        implementation 'com.github.Sheedon:SerialRetrofit:1.0.0'
	}
```



### Maven

**Step 1.** Add the JitPack repository to your build file

```
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

**Step 2.** Add the dependency

```
	<dependency>
	    <groupId>com.github.Sheedon</groupId>
	    <artifactId>SerialRetrofit</artifactId>
	    <version>1.0.0</version>
	</dependency>
```



#### 1. Create SerialClient

```java
SerialClient client = new SerialClient.Builder()
                .path("/tty/s4") // 设备端口 
    		   .baudRate(9600)// 波特率
                .name("qrcode")// 设备名称（自定义）
                .addConverterFactory(DataConverterFactory.create())// 内容转化工厂
                .build();
```



#### 2.  Create Retrofit

```java
Retrofit retrofit = new Retrofit.Builder()
                .client(client)// 串口客户端
                .addConverterFactory(SerialConverterFactory.create())// 设备转化工厂
                .baseStartBit("7A")// 基础起始位
                .baseEndBit("")// 基础停止位
                .build();
```



#### 3. Create Interface

```java
interface RemoteService {


    @STARTBIT("7A")// 起始位
    @MESSAGEBIT("123456")// 消息位
    @PARITYBIT("1111")// 校验位
    @ENDBIT("")// 停止位
    @BACKNAME("get_manager_list")// 反馈内容
    Call<String> getManagerList();


    @MESSAGEBIT("{length}{address}{type}{message}")
    @BACKNAME("0101")
    Call<BoxModel> getManagerList(@Path("length") String length,// 替换消息位内容
                                  @Path("address") String address,
                                  @Path("type") String type,
                                  @Path("message") String message);
}
```

```java
// 将接口传入retrofit中，进行动态代理
RemoteService remoteService = retrofit中，进行动态代理.create(RemoteService.class)
```



#### 4. Use And Dispatcher

```java
// 调度方法
Call<BoxModel> managerList = remoteService.getManagerList("0800", "02", "03", "01");
// 执行方法
managerList.enqueue(new Callback<BoxModel>() {
	@Override
	public void onResponse(Call<BoxModel> call, Response<BoxModel> response) {
		Log.v("SXD",""+response.body());
	}

	@Override
    public void onFailure(Call<BoxModel> call, Throwable t) {
		Log.v("SXD",""+t);
	}
});
```
```
/**
* 反馈
* 设置反馈规则
* RULES 根据反馈的消息体，依次按数据解析内容
*/
public class BoxModel {

    @RULES(length = 2)// 
    private String name;
    @RULES(length = 2)
    private String age;
    @RULES(length = 6)
    private String other;

    @RULES(length = 2)
    private Body body;

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getOther() {
        return other;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "BoxModel{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", other='" + other + '\'' +
                ", body=" + body +
                '}';
    }

    private class Body{
        @RULES(length = 2)
        private String id;

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
```