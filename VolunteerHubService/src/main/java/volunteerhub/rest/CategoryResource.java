package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import volunteerhub.domain.Category;
import volunteerhub.service.ApplicationState;

import java.util.List;

/**
 * Catalogue of the categories offered by the application.
 * Events and volunteer preferences can only use categories from this catalogue.
 * <pre>
 * GET  /api/categories   list
 * POST /api/categories   add a category
 * </pre>
 */
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Category> getCategories() {
        return state.getCategories();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCategory(Category category) {
        return Response.status(Response.Status.CREATED).entity(state.addCategory(category)).build();
    }
}
