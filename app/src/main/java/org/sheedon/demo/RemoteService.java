package org.sheedon.demo;


import org.sheedon.serial.retrofit.Call;
import org.sheedon.serial.retrofit.Observable;
import org.sheedon.serial.retrofit.serialport.BACKNAME;
import org.sheedon.serial.retrofit.serialport.ENDBIT;
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
    @ENDBIT("")
    @BACKNAME("get_manager_list")
    Call<String> getManagerList();

    @BACKNAME("0101")
    Observable<BoxModel> getManagerList1();


    @MESSAGEBIT("{length}{address}{type}{message}")
    @BACKNAME("0101")
    Call<BoxModel> getManagerList(@Path("length") String length,
                                  @Path("address") String address,
                                  @Path("type") String type,
                                  @Path("message") String message);



    /**
     * 绑定rfid指令反馈
     */
    @BACKNAME("01FF")
    Observable<Void> bindCommandBack();


    /**
     * 绑定rfid反馈
     */
    @BACKNAME("0222")
    Observable<RFIDModel> bindRFID();

    /**
     * 发送设置信号强度指令指令
     */
    @MESSAGEBIT("00B6000207D0")
    @PARITYBIT("8F")
    Call<Void> setSignalStrength();

    /**
     * 发送连续读取指令指令
     */
    @MESSAGEBIT("0027000322FFFF")
    @PARITYBIT("4A")
    Call<Void> sendContinuousRead();
}
