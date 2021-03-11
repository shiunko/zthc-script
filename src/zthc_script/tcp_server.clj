(ns zthc-script.tcp-server
  (:import
    [io.netty.buffer ByteBuf]
    [io.netty.channel ChannelHandler ChannelInboundHandlerAdapter ChannelInitializer ChannelInitializer ChannelHandlerContext ChannelFutureListener]
    [io.netty.channel.nio NioEventLoopGroup]
    [io.netty.bootstrap ServerBootstrap]
    [io.netty.channel.socket.nio NioServerSocketChannel])
  (:refer [zthc-script.util :refers [log]]))

(defrecord Server [group channel-future])

(defn last-str
  [^String text x]
  (if (> (count text) x)
    (subs text (-> text count (- x)))
    text)
  )

(defn echo-handler []
  (proxy [ChannelInboundHandlerAdapter]
         []
    (channelRead [^ChannelHandlerContext ctx msg]
      (let [buf ^ByteBuf msg
            len (.readableBytes buf)
            barray (byte-array len)
            client (-> ctx .channel .remoteAddress)
            _  (-> buf (.getBytes 0 barray))
            body (-> barray String.)]
        ;todo
      ))
    (channelReadComplete [^ChannelHandlerContext ctx]
      )
    (handlerAdded [^ChannelHandlerContext ctx]
      (log :info (str "[JOIN][" (-> ctx .channel .remoteAddress) "]")))
    (handlerRemoved [^ChannelHandlerContext ctx]
      (log :info (str "[EXIT][" (-> ctx .channel .remoteAddress) "]"))
      )
    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (log :error "%s" cause)
      (.close ctx))))


(defn close-server [{:keys [group channel-future]}]
  (-> channel-future .channel .closeFuture)
  (.shutdownNow group))

(defn ^ChannelInitializer channel-initializer []
  (proxy [ChannelInitializer]
         []
    (initChannel [ch]
      (-> ch (.pipeline) (.addLast (into-array ChannelHandler [(echo-handler)])))
      )))

(defn start-server
  ([port host]
   (let [group (NioEventLoopGroup.)
         b (ServerBootstrap.)
         ]
     (-> b (.group group)
         (.channel NioServerSocketChannel)
         (.localAddress host port)
         (.childHandler (channel-initializer))
         )
     (->Server group (-> b .bind .sync))))
  ([port]
   (start-server port "127.0.0.1"))
  )
