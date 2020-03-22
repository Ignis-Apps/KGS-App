package de.kgs.vertretungsplan.coverPlan;

public enum GradeSubClass {
    ALL("Alle", "", 0),
    A("a", "a", 1),
    B("b", "b", 2),
    C("c", "c", 3),
    D("d", "d", 4),
    E("e", "e", 5);

    private final String classInitials;
    private final int classLevel;
    private final String className;

    GradeSubClass(String className, String classInitials, int classLevel) {
        this.className = className;
        this.classInitials = classInitials;
        this.classLevel = classLevel;
    }

    public static GradeSubClass getByClassName(String className) {
        for (GradeSubClass subClass : values()) {
            if (subClass.className.equals(className)) {
                return subClass;
            }
        }
        throw new AssertionError("Requested class name does not exist !" + className);
    }

    public static GradeSubClass getClassByClassLevel(int classLevel) {

        for (GradeSubClass subClass : values()) {
            if (subClass.classLevel == classLevel)
                return subClass;
        }
        throw new AssertionError("Requested class level does not exist !");
    }

    public int getClassLevel() {
        return this.classLevel;
    }

    public String getClassName() {
        return this.className;
    }

    public String getClassInitials() {
        return this.classInitials;
    }
}
