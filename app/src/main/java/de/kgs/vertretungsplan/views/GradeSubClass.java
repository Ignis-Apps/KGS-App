package de.kgs.vertretungsplan.views;

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

    public int getClassLevel() {
        return this.classLevel;
    }

    public String getClassName() {
        return this.className;
    }

    public String getClassInitials() {
        return this.classInitials;
    }

    public static GradeSubClass getByClassName(String className) {
        for (GradeSubClass subClass : values()) {
            if (subClass.className.equals(className)) {
                return subClass;
            }
        }
        return null;
    }
}
