package volunteerhub.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Category;

import java.util.List;

@Named
@ApplicationScoped
public class AppData {

    @Inject
    private VolunteerHubService service;

    private List<Category> categories;

    @PostConstruct
    public void init() {
        loadCategories();
    }

    public void loadCategories() {
        categories = service.getAllCategories();
    }

    public List<Category> getCategories() {
        return categories;
    }
}