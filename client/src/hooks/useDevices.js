import {useContext} from "react";
import DevicesContext from "../context/DevicesContext.jsx";

const useDevices = () => {
    return useContext(DevicesContext);
};

export default useDevices;