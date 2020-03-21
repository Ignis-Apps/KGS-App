package de.kgs.vertretungsplan.views;


public enum Grade {
    ALL("Alle Klassen", "", 0),
    FIFTH_CLASS("5. Klasse", "5", 1),
    SIXTH_CLASS("6. Klasse", "6", 2),
    SEVENTH_CLASS("7. Klasse", "7", 3),
    EIGHTH_CLASS("8. Klasse", "8", 4),
    NINTH_CLASS("9. Klasse", "9", 5),
    TENTH_CLASS("10. Klasse", "10", 6),
    J1("Jahrgangsstufe 1", "J1", 7),
    J2("Jahrgangsstufe 2", "J2", 8);

    private final String gradeInitials;
    private final int gradeLevel;
    private final String gradeName;

    private Grade(String gradeName2, String gradeInitials2, int gradeLevel2) {
        this.gradeLevel = gradeLevel2;
        this.gradeName = gradeName2;
        this.gradeInitials = gradeInitials2;
    }

    public boolean hasSubClasses() {
        return this != J1 && this != J2;
    }

    public int getGradeLevel() {
        return this.gradeLevel;
    }

    public String getGradeName() {
        return this.gradeName;
    }

    public String getGradeInitials() {
        return this.gradeInitials;
    }

    public static Grade getGradeByName(String gradeName2) {
        Grade[] values;
        for (Grade grade : values()) {
            if (grade.getGradeName().equals(gradeName2)) {
                return grade;
            }
        }
        return null;
    }
}
