<div class="container-fluid">
    <div class="row toolbar-pf">
        <div class="col-sm-12">
            <form class="toolbar-pf-actions" id="filter-form">
                <div class="form-group toolbar-pf-filter" id="filter-div">
                    <label class="sr-only filter-by" for="filter"><!-- Dynamically load content here --></label>
                    <div class="input-group">
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                    aria-haspopup="true"
                                    aria-expanded="false"><span class="filter-by"><!--Dynamically load content here --></span> <span class="caret"></span></button>
                            <ul class="dropdown-menu">
                                <!-- Dynamically load content here -->
                            </ul>
                        </div><!-- /btn-group -->
                        <input type="text" class="form-control" id="filter" placeholder="Filter by name..." autocomplete="off" title="">
                        <div class="input-group-btn" id="filter-type">
                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                    aria-haspopup="true"
                                    aria-expanded="false"><span class="filter-type"><!--Dynamically load content here --></span> <span class="caret"></span></button>
                            <ul class="dropdown-menu">
                                <!-- Dynamically load content here -->
                            </ul>
                        </div><!-- /btn-group -->
                    </div><!-- /input-group -->
                </div>
                <!-- sort -->
                <div class="form-group" id="sort">
                    <div class="dropdown btn-group">
                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                aria-haspopup="true"
                                aria-expanded="false"
                        ><span id="sort-by"><!-- Dynamically load content here --></span> <span class="caret"></span></button>
                        <ul class="dropdown-menu">
                            <!-- Dynamically load content here -->
                        </ul>
                    </div>
                    <button class="btn btn-link" type="button" id="sort-order">
                        <span class="fa fa-sort-alpha-asc"></span>
                    </button>
                </div>
            </form>
            <div class="row collapse" id="searchTermError">
                <div class="col-sm-4 alert alert-warning" role="alert">
                    <button type="button" class="close" data-toggle="collapse" data-target="#searchTermError" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <strong>Warning!</strong> Invalid search regular expression!
                </div>
            </div>
            <div class="row toolbar-pf-results hidden">
                <div class="col-sm-12">
                    <h5><span id="count-results"><!-- Dynamically load content here 40 Results --></span> Results</h5>
                    <p>Active filters:</p>
                    <ul class="list-inline" id="active-filters">
                        <!-- Dynamically load content here -->
                        <!--
                        <li>
                            <span class="label label-info">
                              Name: nameofthething
                              <a href="#"><span class="pficon pficon-close"></span></a>
                            </span>
                        </li>
                        -->
                    </ul>
                    <p><a href="#" id="clear-filters">Clear All Filters</a></p>
                </div><!-- /col -->
            </div><!-- /row -->
        </div><!-- /col -->
    </div><!-- /row -->
</div>
<script src="reports/resources/js/windup-application-list-sorting.js"></script>
