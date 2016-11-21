package org.apache.mesos.chronos.utils

import java.util.concurrent.locks.{ ReentrantLock, ReentrantReadWriteLock }

class RichLock(val lock: ReentrantLock) extends AnyVal {
  def apply[T](f: => T): T = {
    lock.lock()
    try {
      f
    } finally {
      lock.unlock()
    }
  }
}

object RichLock {
  def apply(fair: Boolean = true): RichLock = new RichLock(new ReentrantLock(fair))
  def apply(lock: ReentrantLock): RichLock = new RichLock(lock)
}

class Lock[T](private val value: T, fair: Boolean = true) {
  private val lock = RichLock(fair)

  def apply[R](f: T => R): R = lock {
    f(value)
  }

  override def equals(o: Any): Boolean = o match {
    case r: Lock[T] => lock {
      r.lock {
        value.equals(r.value)
      }
    }
    case r: T @unchecked => lock {
      value.equals(r)
    }
    case _ => false
  }

  override def hashCode(): Int = lock(value.hashCode())

  override def toString: String = lock {
    value.toString
  }
}

object Lock {
  def apply[T](value: T, fair: Boolean = true): Lock[T] = new Lock(value, fair)
}