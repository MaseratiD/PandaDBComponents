package cn.pandadb.util

/**
  * Created by bluejoe on 2019/10/9.
  */
import org.slf4j.LoggerFactory

trait Logging {
  val logger = LoggerFactory.getLogger(this.getClass);
}
