import {useContext} from "react";
import OwnershipsContext from "../context/OwnershipContext.jsx";

const useOwnerships = () => {
    return useContext(OwnershipsContext);
};

export default useOwnerships;