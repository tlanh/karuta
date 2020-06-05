package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class PortfolioGroupRepositoryTest {
    @Autowired
    private PortfolioGroupRepository repository;

    @Test
    public void findByLabel() {
        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setType("");
        portfolioGroup.setLabel("foo");

        repository.save(portfolioGroup);

        Optional<PortfolioGroup> found = repository.findByLabel("foo");

        assertTrue(found.isPresent());
        assertEquals(portfolioGroup.getId(), found.get().getId());
    }

    @Test
    public void existsByIdAndType() {
        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setType("foo");
        portfolioGroup.setLabel("");

        repository.save(portfolioGroup);

        assertTrue(repository.existsByIdAndType(portfolioGroup.getId(), "foo"));

        assertFalse(repository.existsByIdAndType(portfolioGroup.getId(), "bar"));
        assertFalse(repository.existsByIdAndType(-12L, "foo"));
    }
}