package com.nonBlocking.service;

import com.nonBlocking.common.ErrorCode;
import com.nonBlocking.common.ResponseData;
import com.nonBlocking.controller.APIController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Component
public class SocketService {
    static private Logger log = LoggerFactory.getLogger(SocketService.class);
    String errorMsg = "";
    int errorCode = ErrorCode.SUCCESS;
    ApplicationContext responseDataAC = new AnnotationConfigApplicationContext(ResponseData.class);
    ResponseData responseData = responseDataAC.getBean(ResponseData.class);
    static Selector selector = null;
    static SocketChannel socketChannel = null;
    static SocketChannel blockingSocketChannel = null;

    // 초기화
    public void BlockingSocketInit(String ip, int port) {
        log.info("Call BlockingSocketInit.");

        try {
            blockingSocketChannel = SocketChannel.open();
            blockingSocketChannel.configureBlocking(true);
            blockingSocketChannel.connect(new InetSocketAddress(ip, port));
        } catch (IOException e) {
            errorMsg = e.getLocalizedMessage();
            errorCode = ErrorCode.CONNECTION_ERROR;
            log.error("[{}] {}",errorCode, errorMsg);
        }
    }

    // 쓰기와 읽기
    public ResponseData BlockingSocket(String body) {
        log.info("Call BlockingSocket.");
        BlockingSocketInit("localhost", 28080);
        try {
            // Response Server 호출
            ByteBuffer byteBuffer = ByteBuffer.wrap(body.getBytes());
            blockingSocketChannel.write(byteBuffer);

            BlockingSocketRead();

            blockingSocketChannel.close();
        } catch (IOException e) {
            errorMsg = e.getLocalizedMessage();
            errorCode = ErrorCode.CONNECTION_ERROR;
            log.error("[{}] {}",errorCode, errorMsg);
        }

        responseData.setErrorCode(errorCode);
        responseData.setErrorMsg(errorMsg);
        responseData.setBody(body);

        return responseData;
    }

    // 읽기
    public void BlockingSocketRead() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        int readInt = 0;
        try {
            // 여기서 Block
            readInt = blockingSocketChannel.read(byteBuffer);
            if (-1 != readInt) {
                Charset charset = Charset.forName("UTF-8");
                byteBuffer.flip();
                String body = charset.decode(byteBuffer).toString();
                log.info("body : {}", body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void NonBlockingSocketInit(String ip, int port) {
        log.info("Call NonBlockingSocketInit Method.");
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open(new InetSocketAddress(ip, port));
            socketChannel.configureBlocking(false);
            // Channel에 Selector 등록
            socketChannel.register(selector, SelectionKey.OP_READ, new StringBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void NonBlockingSocketWrite(String body) {
        log.info("Call NonBlockingSocketWrite Method.");

        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(body.getBytes());
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            errorMsg = e.getLocalizedMessage();
            errorCode = ErrorCode.CONNECTION_ERROR;
            log.error("[{}] {}",errorCode, errorMsg);
        }
    }

    public static class Receive implements Runnable {
        public void run() {
            try {
                while(true) {
                    selector.select();
                    Iterator iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = (SelectionKey) iterator.next();

                        if(key.isReadable()) {
                            read(key);
                        }

                        iterator.remove();
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private String read(SelectionKey key) {
            log.info("Call read method.");
            String body = "";
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(128);
            int readInt = 0;
            try {
                readInt = ((SocketChannel)key.channel()).read(byteBuffer);
                if (-1 != readInt) {
                    Charset charset = Charset.forName("UTF-8");
                    byteBuffer.flip();
                    body = charset.decode(byteBuffer).toString();
                    log.info("body : {}", body);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return body;
        }
    }
}
