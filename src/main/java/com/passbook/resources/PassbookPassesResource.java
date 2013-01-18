package com.passbook.resources;

import com.google.common.base.Optional;
import com.passbook.api.Pass;
import com.passbook.core.Device;
import com.passbook.db.DeviceDAO;
import com.passbook.db.RegistrationDAO;
import com.passbook.helper.Authenticator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/v1/passes")
@Produces(MediaType.APPLICATION_JSON)
public class PassbookPassesResource {

    private final DeviceDAO deviceDAO;

    public PassbookPassesResource(final DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    @GET
    @Path("/{passTypeIdentifier}/{serialNumber}")
    public Response getLatest(@Context HttpHeaders headers,
                              @PathParam("passTypeIdentifier") String passTypeIdentifier,
                              @PathParam("serialNumber") String serialNumber) {
        Optional<Device> device = deviceDAO.findByPassTypeIdentifierAndSerialNumber(passTypeIdentifier, serialNumber);
        if (!device.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!Authenticator.isAuthorized(headers, device.get())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return  Response.ok(new Pass(device.get().getUpdatedAt(), serialNumber))
                        .lastModified(new DateTime(device.get().getUpdatedAt(), DateTimeZone.UTC).toDate())
                        .build();
    }

}
