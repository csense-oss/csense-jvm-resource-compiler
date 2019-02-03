-injars       /build/libs/resource-compiler-1.0-SNAPSHOT.jar
-outjars      /build/libs/resource-compiler-1.0-SNAPSHOT-optimized.jar
-libraryjars  <java.home>/lib/rt.jar
-printmapping myapplication.map


-verbose
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep public class csense.javafx.resource.compiler.MainKt {
      public static void main(java.lang.String[]);
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
