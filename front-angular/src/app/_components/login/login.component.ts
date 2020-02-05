import { Component, OnInit } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {TokenStorageService} from "../../_services/token-storage.service";
import {AuthService} from "../../_services/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService,private storage:TokenStorageService) { }
  form: any = {};
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  roles: string[] = [];

  ngOnInit() {
    if (this.storage.getToken()){
      this.isLoggedIn=true;
      this.roles=this.storage.getUser().roles;
      console.log(this.roles,"111111111111111111111111111111111")
    }
  }

    onSubmit(){
      this.authService.login(this.form).subscribe(data =>{
        this.storage.saveToken(data.accessToken);
        this.storage.saveUser(data);

        this.isLoggedIn=true;
        this.isLoginFailed=false;
        this.roles=this.storage.getUser().roles;
        this.reLoadPage();

      },err => {
        this.errorMessage=err.error.message;
        this.isLoginFailed=true;
      })
    }
    reLoadPage(){
      window.location.reload();
    }
}
