package com.github.alexeyr.trace.entry_exit

import org.apache.logging.log4j.Logger

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox.Context

sealed trait LoggedArgs
object LoggedArgs {
  case object None extends LoggedArgs
  case class Some(args: List[String]) extends LoggedArgs
  case object All extends LoggedArgs
}

sealed trait LoggingLibrary
object LoggingLibrary {
  case object SLF4J extends LoggingLibrary
  case object Log4j2 extends LoggingLibrary
}

@compileTimeOnly("Enable macro paradise to expand macro annotations")
class TraceEntryExit(loggedArgs: LoggedArgs = LoggedArgs.All, logReturnValue: Boolean = true, logTime: Boolean = false, loggerFieldName: String = "logger", library: LoggingLibrary = LoggingLibrary.SLF4J) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro TraceEntryExitImpl.macroImpl
}

// Note: must have the same parameters with the same defaults as TraceEntryExit
case class TraceEntryExitArgs(loggedArgs: LoggedArgs = LoggedArgs.All, logReturnValue: Boolean = true, logTime: Boolean = false, loggerFieldName: String = "logger", library: LoggingLibrary = LoggingLibrary.SLF4J)

@compileTimeOnly("SkipTracing can only be used inside a class/trait/object annotated with @TraceEntryExit")
class SkipTracing extends StaticAnnotation

class TraceEntryExitImpl(val c: Context) {
  import c.universe._

  def traceBody(body: List[Tree], trace: TraceEntryExitArgs) = ???

  def traceMethod(md: DefDef, trace: TraceEntryExitArgs) = md match {
    case DefDef(mods, name, typeParams, paramss, tpt, rhs) =>
      val markerClassName = trace.library match {
        case LoggingLibrary.SLF4J => Slf4jMarkers.getClass.getName.stripSuffix("$")
        case LoggingLibrary.Log4j2 => classOf[Logger].getName
      }
      val markerClass = markerClassName.split("""\.""").foldLeft[Tree](q"_root_") { (parent, name) => Select(parent, TermName(name)) }
      val logger = TermName(trace.loggerFieldName)
      // used in concatenation with other string literals which should get optimized
      val nameLiteral = Literal(Constant(name.toString))

      def logStmt(markerName: String, msg: Tree) = {
        val marker = q"$markerClass.${TermName(markerName)}"
        q"if ($logger.isTraceEnabled($marker)) { logger.trace($marker, $msg) }"
      }

      val entryLogStmt = {
        // TODO args
        val msg = q""" "> Entering method " + $nameLiteral """
        logStmt("ENTRY_MARKER", msg)
      }
      val result = c.freshName(TermName("result"))
      val exitLogStmt = {
        val msg =
          if (trace.logReturnValue)
            q""" "< Exiting method " + $nameLiteral + "; returning " + String.valueOf($result)"""
          else
            q""" "< Exiting method " + $nameLiteral """
        logStmt("EXIT_MARKER", msg)
      }
      val newRhs = rhs match {
        case q"{..$stmts}" =>
          q"{$entryLogStmt; val $result = {..$stmts}; $exitLogStmt; $result}"
      }
      DefDef(mods, name, typeParams, paramss, tpt, newRhs)
  }

  def treeToArgs(tree: Tree) = tree match {
    case q"new TraceEntryExit(...$args)" =>
      val argsClass = TypeName(classOf[TraceEntryExitArgs].getName)
      val argsTree = q"new com.github.alexeyr.trace.entry_exit.TraceEntryExitArgs(...$args)"
      c.eval(c.Expr[TraceEntryExitArgs](argsTree))
  }

  def macroImpl(annottees: c.Tree*): c.Tree = {
    val annotation = treeToArgs(c.prefix.tree)

    annottees.toList match {
      case ClassDef(mods, name, typeParams, Template(parents, self, body)) :: rest =>
        val newBody = traceBody(body, annotation)

        val transformed = ClassDef(mods, name, typeParams, Template(parents, self, newBody))

        rest match {
          case Nil =>
            transformed
          case _ =>
            q"{$transformed; ..$rest}"
        }
      case ModuleDef(mods, name, Template(parents, self, body)) :: Nil =>
        val newBody = traceBody(body, annotation)

        ModuleDef(mods, name, Template(parents, self, newBody))
      case (md: DefDef) :: Nil =>
        traceMethod(md, annotation)
      case _ =>
        c.abort(c.enclosingPosition, "@TraceEntryExit can annotate only classes and methods")
    }
  }
}
