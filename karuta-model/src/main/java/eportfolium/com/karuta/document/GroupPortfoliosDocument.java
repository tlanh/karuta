package eportfolium.com.karuta.document;

import eportfolium.com.karuta.model.bean.Portfolio;

import java.util.List;

public class GroupPortfoliosDocument {
    private Long id;
    private List<Portfolio> portfolios;

    public GroupPortfoliosDocument(Long id, List<Portfolio> portfolios) {
        this.id = id;
        this.portfolios = portfolios;
    }

    public Long getId() {
        return id;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }
}
