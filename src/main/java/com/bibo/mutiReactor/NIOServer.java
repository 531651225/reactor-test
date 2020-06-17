package com.bibo.mutiReactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
//多Reactor
public class NIOServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);

  public static void main(String[] args) throws IOException {
    //创建Selector通道管理器
    Selector selector = Selector.open();
    // 获得一个ServerSocket通道
    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    // 设置通道为 非阻塞
    serverSocketChannel.configureBlocking(false);
    // 将该通道对于的serverSocket绑定到port端口
    serverSocketChannel.bind(new InetSocketAddress(1234));
    // 将通道管理器和该通道绑定，并为该通道注册selectionKey.OP_ACCEPT事件  
    // 注册该事件后，当事件到达的时候，selector.select()会返回，  
    // 如果事件没有到达selector.select()会一直阻塞 
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    
    Processor[] processors = new Processor[1];
    for(int i = 0; i < processors.length; i++) {
      processors[i] = new Processor();
//      processors[i].start();
    }

    // 当注册事件到达时，方法返回，否则该方法会一直阻塞
    while (selector.select() > 0) {
      Set<SelectionKey> keys = selector.selectedKeys();
      for (SelectionKey key : keys) {
        // 删除已选的key 以防止反复处理,下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
        keys.remove(key);
        // 客户端请求连接事件
        if (key.isAcceptable()) {
          ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();
          // 获得和客户端连接的通道
          SocketChannel socketChannel = acceptServerSocketChannel.accept();
          // 设置成非阻塞
          socketChannel.configureBlocking(false);
          LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
//          Processor processor = processors[(int)((index++)/1)];
          Processor processor = processors[0];
          processor.addChannel(socketChannel);
        }
      }
    }
  }

}
