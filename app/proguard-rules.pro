# ProGuard rules for the Sudoku application.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# Hilt and Dagger rules (usually packaged, but good as a fallback)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep serializable model states if needed
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
