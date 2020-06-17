package com.bibo.mutiReactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Processor {
  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private static final ExecutorService service = Executors.newFixedThreadPool(1);

  private Selector selector;
  private boolean isRunning;
  
  public Processor() throws IOException {
    this.selector = SelectorProvider.provider().openSelector();
  }
  public void addChannel (SocketChannel socketChannel) throws ClosedChannelException {
    // 在客户端 连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限
    socketChannel.register(this.selector, SelectionKey.OP_READ);
    if(!isRunning) {
      start();
      isRunning = true;
    }
  }
  
  public void start() {
    service.submit(() -> {
      while(true) {
        //不会阻塞，不管什么通道就绪都立刻返回
        //译者注：此方法执行非阻塞的选择操作。
        // 如果自从前一次选择操作后，没有通道变成可选择的，则此方法直接返回零
        if(selector.selectNow() <= 0) {
          continue;
        }
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while(iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();
          // 获得了可读的事件
          if(key.isReadable()) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) key.channel();
            int count = socketChannel.read(buffer);
            if (count < 0) {
              socketChannel.close();
              key.cancel();
              LOGGER.info("{}\t Read ended", socketChannel);
              continue;
            } else if(count == 0) {
              LOGGER.info("{}\t Message size is 0", socketChannel);
              continue;
            } else {
              LOGGER.info("{}\t Read message {}", socketChannel, new String(buffer.array()));
            }
          }
        }
      }
    });
  }
  
}
