const API_URL =
    'http://localhost:8080/auth';



/*
|--------------------------------------------------------------------------
| LOGIN
|--------------------------------------------------------------------------
*/

const loginForm =
    document.getElementById('loginForm');



if (loginForm) {

    loginForm.addEventListener(

        'submit',

        async (e) => {

            e.preventDefault();



            const email =
                document.querySelector(
                    'input[name="email"]'
                ).value;



            const password =
                document.querySelector(
                    'input[name="password"]'
                ).value;



            try {

                const response =
                    await fetch(

                        `${API_URL}/login`,

                        {
                            method: 'POST',

                            headers: {

                                'Content-Type':
                                    'application/json'
                            },

                            body: JSON.stringify({

                                email,
                                password
                            })
                        }
                    );



                const data =
                    await response.json();



                if (!response.ok) {

                    throw new Error(

                        data.message ||

                        'Invalid credentials'
                    );
                }



                /*
                 |--------------------------------------------------------------------------
                 | SAVE SESSION
                 |--------------------------------------------------------------------------
                 */

                sessionStorage.setItem(
                    'token',
                    data.token
                );

                sessionStorage.setItem(
                    'role',
                    data.role
                );

                sessionStorage.setItem(
                    'email',
                    data.email
                );



                /*
                 |--------------------------------------------------------------------------
                 | ROLE REDIRECT
                 |--------------------------------------------------------------------------
                 */

                if (data.role === 'ADMIN') {

                    window.location.href =
                        '/admin/home';
                }

                else if (
                    data.role === 'ORGANIZER'
                ) {

                    window.location.href =
                        '/organizer/home';
                }

                else {

                    window.location.href =
                        '/student/home';
                }
            }

            catch (error) {

                showMessage(
                    error.message,
                    false
                );
            }
        }
    );
}



/*
|--------------------------------------------------------------------------
| REGISTER
|--------------------------------------------------------------------------
*/

const registerForm =
    document.getElementById(
        'registerForm'
    );



if (registerForm) {

    registerForm.addEventListener(

        'submit',

        async (e) => {

            e.preventDefault();



            const body = {

                email:
                    document.querySelector(
                        'input[name="email"]'
                    ).value,



                password:
                    document.querySelector(
                        'input[name="password"]'
                    ).value,



                role:
                    document.querySelector(
                        'select[name="role"]'
                    ).value,



                /*
                 |--------------------------------------------------------------------------
                 | IDS
                 |--------------------------------------------------------------------------
                 */

                studentId:
                    document.querySelector(
                        'input[name="studentId"]'
                    )?.value || null,



                employeeId:
                    document.querySelector(
                        'input[name="employeeId"]'
                    )?.value || null
            };



            try {

                const response =
                    await fetch(

                        `${API_URL}/register`,

                        {
                            method: 'POST',

                            headers: {

                                'Content-Type':
                                    'application/json'
                            },

                            body: JSON.stringify(body)
                        }
                    );



                const data =
                    await response.json();



                if (!response.ok) {

                    throw new Error(

                        data.message ||

                        'Registration failed'
                    );
                }



                showMessage(
                    'Account created successfully',
                    true
                );



                setTimeout(() => {

                    window.location.href =
                        '/login';

                }, 1500);
            }

            catch (error) {

                showMessage(
                    error.message,
                    false
                );
            }
        }
    );
}



/*
|--------------------------------------------------------------------------
| LOGOUT
|--------------------------------------------------------------------------
*/

function logout() {

    sessionStorage.clear();

    window.location.href =
        "/login";
}



/*
|--------------------------------------------------------------------------
| MESSAGE
|--------------------------------------------------------------------------
*/

function showMessage(
    message,
    success
) {

    const messageBox =
        document.getElementById(
            'message'
        );



    if (!messageBox) return;



    messageBox.innerText =
        message;



    messageBox.style.color =
        success
            ? '#4ade80'
            : '#f87171';
}