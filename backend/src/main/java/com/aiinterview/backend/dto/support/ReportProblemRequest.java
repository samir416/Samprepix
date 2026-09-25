package com.aiinterview.backend.dto.support;

public class ReportProblemRequest {

    private String feature;
    private String description;
    private String actionAttempted;
    private String reporterEmail;
    private String pageUrl;

    public ReportProblemRequest() {
    }

    public ReportProblemRequest(String feature, String description, String actionAttempted, String reporterEmail, String pageUrl) {
        this.feature = feature;
        this.description = description;
        this.actionAttempted = actionAttempted;
        this.reporterEmail = reporterEmail;
        this.pageUrl = pageUrl;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionAttempted() {
        return actionAttempted;
    }

    public void setActionAttempted(String actionAttempted) {
        this.actionAttempted = actionAttempted;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
}
