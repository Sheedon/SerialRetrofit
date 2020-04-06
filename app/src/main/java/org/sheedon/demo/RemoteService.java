package org.sheedon.demo;


import org.sheedon.serial.retrofit.Call;
import org.sheedon.serial.retrofit.serialport.BACKNAME;
import org.sheedon.serial.retrofit.serialport.MESSAGEBIT;
import org.sheedon.serial.retrofit.serialport.PARITYBIT;
import org.sheedon.serial.retrofit.serialport.Path;
import org.sheedon.serial.retrofit.serialport.STARTBIT;

/**
 * @Description: java类作用描述
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/27 16:20
 */
interface RemoteService {


    @STARTBIT("7A")
    @MESSAGEBIT("123456")
    @PARITYBIT("1111")
    @BACKNAME("get_manager_list")
    Call<String> getManagerList();


    @MESSAGEBIT("{length}{address}{type}{message}")
    @BACKNAME("0101")
    Call<BoxModel> getManagerList(@Path("length") String length,
                                  @Path("address") String address,
                                  @Path("type") String type,
                                  @Path("message") String message);
}
