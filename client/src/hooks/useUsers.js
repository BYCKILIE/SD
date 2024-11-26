import {useContext} from "react";
import UsersContext from "../context/UsersContext.jsx";

const useUsers = () => {
    return useContext(UsersContext);
};

export default useUsers;