package setup

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

class BindingSetup extends Module {

  override def bindings(environment: Environment, configuration: Configuration): collection.Seq[Binding[_]] = {
    Seq(
      bind[Executor].toSelf.eagerly()
    )
  }

}
