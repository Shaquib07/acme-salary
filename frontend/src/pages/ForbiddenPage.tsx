import { Button, Typography } from "@mui/material";
import { Link } from "react-router-dom";

export function ForbiddenPage() {
  return (
    <>
      <Typography variant="h5">You cannot do that</Typography>
      <Typography sx={{ my: 2 }}>Your role does not allow this action. The API would reject it too.</Typography>
      <Button component={Link} to="/directory" variant="contained">
        Back to Employee Details
      </Button>
    </>
  );
}
