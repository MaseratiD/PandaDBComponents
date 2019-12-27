package cn.pandadb.rpc

import cn.pandadb.network.internal.message.{InternalRpcRequest, InternalRpcResponse}

import scala.collection.mutable.ArrayBuffer
import cn.pandadb.util.Logging
import net.neoremind.kraps.RpcConf
import net.neoremind.kraps.rpc.{RpcCallContext, RpcEndpoint, RpcEnv, RpcEnvServerConfig}
import net.neoremind.kraps.rpc.netty.NettyRpcEnvFactory

/**
  * @Author: Airzihao
  * @Description: copy bluejoe's code.
  * @Date: Created in 18:18 2019/12/26
  * @Modified By:
  */
class Neo4jAgentRPCServer(host: String, port: Int, serverName: String) extends Logging{
  val config = RpcEnvServerConfig(new RpcConf(), serverName, host, port)
  val thisRpcEnv = NettyRpcEnvFactory.create(config)
  val handlers = ArrayBuffer[PartialFunction[InternalRpcRequest, InternalRpcResponse]]()

  val endpoint: RpcEndpoint = new RpcEndpoint {
    override val rpcEnv: RpcEnv = thisRpcEnv

    override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
      case request: InternalRpcRequest =>
        val response = handlers.find {
          _.isDefinedAt(request)
        }.map(_.apply(request)).get
        context.reply(response)
    }
  }

  def accept(handler: PartialFunction[InternalRpcRequest, InternalRpcResponse]): Unit = {
    handlers += handler;
  }

  def accept(handler: RequestHandler): Unit = {
    handlers += handler.logic;
  }

  def start(onStarted: => Unit = {}) {
    thisRpcEnv.setupEndpoint(s"PNodeRpc-service", endpoint)
    onStarted;
    thisRpcEnv.awaitTermination()
  }

  def shutdown(): Unit = {
    thisRpcEnv.shutdown()
  }

}

trait RequestHandler {
  val logic: PartialFunction[InternalRpcRequest, InternalRpcResponse];
}