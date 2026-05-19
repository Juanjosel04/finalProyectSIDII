/* =========================================================
   CREATE EVENT
========================================================= */

const createEventForm =
    document.getElementById(
        "createEventForm"
    );



const messageDiv =
    document.getElementById(
        "message"
    );



/* =========================================================
   HELPERS
========================================================= */

function showMessage(
    text,
    type = "success"
) {

    messageDiv.style.display =
        "block";



    messageDiv.textContent =
        text;



    if (type === "success") {

        messageDiv.style.background =
            "rgba(34,197,94,0.12)";

        messageDiv.style.border =
            "1px solid rgba(34,197,94,0.28)";

        messageDiv.style.color =
            "#86efac";
    }

    else {

        messageDiv.style.background =
            "rgba(239,68,68,0.12)";

        messageDiv.style.border =
            "1px solid rgba(239,68,68,0.28)";

        messageDiv.style.color =
            "#fca5a5";
    }
}



/* =========================================================
   CREATE EVENT SUBMIT
========================================================= */

createEventForm.addEventListener(
    "submit",

    async (e) => {

        e.preventDefault();



        messageDiv.style.display =
            "none";



        /*
         * JWT TOKEN
         */

        const token =
            sessionStorage.getItem(
                "token"
            );



        if (!token) {

            window.location.href =
                "/login";

            return;
        }



        /*
         * FORM VALUES
         */

        const title =
            document.getElementById(
                "title"
            ).value.trim();



        const description =
            document.getElementById(
                "description"
            ).value.trim();



        const type =
            document.getElementById(
                "type"
            ).value;



        const location =
            document.getElementById(
                "location"
            ).value.trim();



        const capacity =
            parseInt(

                document.getElementById(
                    "capacity"
                ).value
            );



        const startDate =
            document.getElementById(
                "startDate"
            ).value;



        const endDate =
            document.getElementById(
                "endDate"
            ).value;



        const tagsInput =
            document.getElementById(
                "tags"
            ).value;



        const metadataInput =
            document.getElementById(
                "metadata"
            ).value;



        /*
         * VALIDATIONS
         */

        if (

            !title ||

            !description ||

            !type ||

            !location ||

            !capacity ||

            !startDate ||

            !endDate
        ) {

            showMessage(
                "Completa todos los campos requeridos.",
                "error"
            );

            return;
        }



        if (capacity <= 0) {

            showMessage(
                "La capacidad debe ser mayor que 0.",
                "error"
            );

            return;
        }



        if (

            new Date(endDate)

            <=

            new Date(startDate)
        ) {

            showMessage(
                "La fecha final debe ser posterior a la inicial.",
                "error"
            );

            return;
        }



        /*
         * TAGS
         */

        const tags =
            tagsInput

                ? tagsInput
                    .split(",")
                    .map(tag => tag.trim())
                    .filter(tag => tag.length > 0)

                : [];



        /*
         * METADATA
         */

        let metadata = {};



        if (
            metadataInput.trim() !== ""
        ) {

            try {

                metadata =
                    JSON.parse(
                        metadataInput
                    );
            }

            catch (error) {

                showMessage(
                    "El metadata JSON no es válido.",
                    "error"
                );

                return;
            }
        }



        /*
         * REQUEST BODY
         */

        const body = {

            title,

            description,

            type,

            location,

            capacity,

            startDate,

            endDate,

            tags,

            metadata
        };



        /*
         * BUTTON LOADING
         */

        const submitButton =
            document.querySelector(
                ".create-event-btn"
            );



        const originalButtonText =
            submitButton.innerHTML;



        submitButton.disabled =
            true;



        submitButton.innerHTML =
            `
                <span>
                    Creando evento...
                </span>
            `;



        try {

            /*
             * API REQUEST
             */

            const response =
                await fetch(

                    "/events",

                    {

                        method: "POST",

                        headers: {

                            "Content-Type":
                                "application/json",

                            "Authorization":
                                `Bearer ${token}`
                        },

                        body:
                            JSON.stringify(body)
                    }
                );



            /*
             * ERROR RESPONSE
             */

            if (!response.ok) {

                const errorText =
                    await response.text();



                throw new Error(
                    errorText
                );
            }



            /*
             * SUCCESS
             */

            const data =
                await response.json();



            showMessage(
                `Evento "${data.title}" creado correctamente.`,
                "success"
            );



            /*
             * RESET FORM
             */

            createEventForm.reset();



            /*
             * OPTIONAL REDIRECT
             */

            setTimeout(() => {

                window.location.href =
                    "/admin/home";

            }, 1800);

        }

        catch (error) {

            console.error(error);



            showMessage(

                "No se pudo crear el evento.",

                "error"
            );
        }

        finally {

            submitButton.disabled =
                false;



            submitButton.innerHTML =
                originalButtonText;
        }
    }
);



/* =========================================================
   AUTO CLOSE MESSAGE
========================================================= */

document.addEventListener(

    "input",

    () => {

        messageDiv.style.display =
            "none";
    }
);