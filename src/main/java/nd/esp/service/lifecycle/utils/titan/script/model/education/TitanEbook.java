package nd.esp.service.lifecycle.utils.titan.script.model.education;

import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanVertex;

/**
 * Created by Administrator on 2016/9/19.
 */
@TitanVertex(label = "ebooks")
public class TitanEbook extends TitanEducation{
    @TitanField(name = "ext_isbn")
    private String isbn;
    @TitanField(name = "ext_attachments")
    private String dbattachments;
    @TitanField(name = "ext_criterion")
    private String criterion;
    @TitanField(name = "ext_source")
    private String dbsource;
    @TitanField(name = "ext_edition")
    private String edition;
    @TitanField(name = "ext_grade")
    private String grade;
    @TitanField(name = "ext_phase")
    private String phase;
    @TitanField(name = "ext_subject")
    private String subject;

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDbattachments() {
        return dbattachments;
    }

    public void setDbattachments(String dbattachments) {
        this.dbattachments = dbattachments;
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public String getDbsource() {
        return dbsource;
    }

    public void setDbsource(String dbsource) {
        this.dbsource = dbsource;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
